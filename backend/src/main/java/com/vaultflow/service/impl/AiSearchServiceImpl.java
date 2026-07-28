package com.vaultflow.service.impl;

import com.vaultflow.dto.request.AiSearchRequest;
import com.vaultflow.dto.response.AiSearchResponse;
import com.vaultflow.dto.response.FileResponse;
import com.vaultflow.dto.response.ParsedSearchFilterDto;
import com.vaultflow.entity.FileItem;
import com.vaultflow.entity.User;
import com.vaultflow.exception.ResourceNotFoundException;
import com.vaultflow.repository.FileRepository;
import com.vaultflow.repository.UserRepository;
import com.vaultflow.repository.specification.FileSpecification;
import com.vaultflow.service.AiSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSearchServiceImpl implements AiSearchService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public AiSearchResponse processAiSearch(String userEmail, AiSearchRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userEmail));

        String prompt = request.getPrompt().trim();
        ParsedSearchFilterDto filter = parseNaturalLanguagePrompt(prompt);

        Instant startDate = null;
        if (filter.getDaysAgo() != null && filter.getDaysAgo() > 0) {
            startDate = Instant.now().minus(filter.getDaysAgo(), ChronoUnit.DAYS);
        }

        Specification<FileItem> spec = FileSpecification.getSearchSpecification(
                user,
                filter.getQuery(),
                filter.getExtension(),
                null,
                startDate,
                null,
                filter.getMinSizeBytes(),
                filter.getMaxSizeBytes()
        );

        List<FileItem> matchingFiles = fileRepository.findAll(spec);

        return AiSearchResponse.builder()
                .originalPrompt(prompt)
                .parsedFilter(filter)
                .matchingFiles(matchingFiles.stream().map(this::mapToFileResponse).toList())
                .build();
    }

    private ParsedSearchFilterDto parseNaturalLanguagePrompt(String prompt) {
        String lower = prompt.toLowerCase();
        String extractedExtension = null;
        String extractedQuery = null;
        Long minSizeBytes = null;
        Long maxSizeBytes = null;
        Integer daysAgo = null;
        List<String> interpretations = new ArrayList<>();

        // 1. Detect file extension tags (pdf, docx, txt, zip, png, jpg, jpeg)
        if (lower.contains("pdf")) {
            extractedExtension = "pdf";
            interpretations.add("Filter: PDF Documents");
        } else if (lower.contains("docx") || lower.contains("word")) {
            extractedExtension = "docx";
            interpretations.add("Filter: Word Documents (.docx)");
        } else if (lower.contains("png") || lower.contains("jpg") || lower.contains("jpeg") || lower.contains("image")) {
            extractedExtension = "png";
            interpretations.add("Filter: Images");
        } else if (lower.contains("zip") || lower.contains("archive")) {
            extractedExtension = "zip";
            interpretations.add("Filter: ZIP Archives");
        } else if (lower.contains("txt") || lower.contains("text")) {
            extractedExtension = "txt";
            interpretations.add("Filter: Text Documents");
        }

        // 2. Detect size constraints (e.g. "larger than 2MB", "bigger than 5MB", "smaller than 1MB", "> 500KB")
        Pattern largerPattern = Pattern.compile("(larger|bigger|greater|more|>)\\s*(than)?\\s*(\\d+)\\s*(mb|kb|gb)", Pattern.CASE_INSENSITIVE);
        Matcher largerMatcher = largerPattern.matcher(lower);
        if (largerMatcher.find()) {
            long sizeVal = Long.parseLong(largerMatcher.group(3));
            String unit = largerMatcher.group(4).toLowerCase();
            minSizeBytes = convertToBytes(sizeVal, unit);
            interpretations.add("Size >= " + sizeVal + " " + unit.toUpperCase());
        }

        Pattern smallerPattern = Pattern.compile("(smaller|less|under|<)\\s*(than)?\\s*(\\d+)\\s*(mb|kb|gb)", Pattern.CASE_INSENSITIVE);
        Matcher smallerMatcher = smallerPattern.matcher(lower);
        if (smallerMatcher.find()) {
            long sizeVal = Long.parseLong(smallerMatcher.group(3));
            String unit = smallerMatcher.group(4).toLowerCase();
            maxSizeBytes = convertToBytes(sizeVal, unit);
            interpretations.add("Size <= " + sizeVal + " " + unit.toUpperCase());
        }

        // 3. Detect date ranges (e.g. "today", "yesterday", "last week", "this month", "last 30 days")
        if (lower.contains("today")) {
            daysAgo = 1;
            interpretations.add("Timeframe: Last 24 Hours");
        } else if (lower.contains("yesterday")) {
            daysAgo = 2;
            interpretations.add("Timeframe: Last 48 Hours");
        } else if (lower.contains("week") || lower.contains("last 7 days")) {
            daysAgo = 7;
            interpretations.add("Timeframe: Last 7 Days");
        } else if (lower.contains("month") || lower.contains("last 30 days")) {
            daysAgo = 30;
            interpretations.add("Timeframe: Last 30 Days");
        }

        // 4. Extract search keywords (remove filler words)
        String cleaned = lower
                .replaceAll("(?i)\\b(find|show|me|my|files|documents|uploaded|created|larger|bigger|smaller|less|than|today|yesterday|week|month|pdf|docx|word|png|jpg|jpeg|zip|txt|image|archive|and|or|in|with)\\b", "")
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .trim();

        if (!cleaned.isEmpty()) {
            extractedQuery = cleaned;
            interpretations.add("Keyword Match: \"" + cleaned + "\"");
        }

        String summary = interpretations.isEmpty()
                ? "Searching across all workspace files"
                : String.join(" | ", interpretations);

        return ParsedSearchFilterDto.builder()
                .query(extractedQuery)
                .extension(extractedExtension)
                .minSizeBytes(minSizeBytes)
                .maxSizeBytes(maxSizeBytes)
                .daysAgo(daysAgo)
                .interpretationSummary(summary)
                .build();
    }

    private long convertToBytes(long value, String unit) {
        return switch (unit.toLowerCase()) {
            case "kb" -> value * 1024L;
            case "mb" -> value * 1024L * 1024L;
            case "gb" -> value * 1024L * 1024L * 1024L;
            default -> value;
        };
    }

    private FileResponse mapToFileResponse(FileItem fileItem) {
        return FileResponse.builder()
                .id(fileItem.getId())
                .originalName(fileItem.getOriginalName())
                .mimeType(fileItem.getMimeType())
                .extension(fileItem.getExtension())
                .sizeBytes(fileItem.getSizeBytes())
                .formattedSize(formatFileSize(fileItem.getSizeBytes()))
                .folderId(fileItem.getFolder() != null ? fileItem.getFolder().getId() : null)
                .isTrashed(fileItem.getIsTrashed())
                .createdAt(fileItem.getCreatedAt())
                .updatedAt(fileItem.getUpdatedAt())
                .build();
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}

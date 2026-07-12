package vaultflow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vaultflow.dto.request.CreateFolderRequest;
import vaultflow.dto.request.MoveRequest;
import vaultflow.dto.request.RenameRequest;
import vaultflow.dto.response.FolderResponse;
import vaultflow.service.FolderService;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FolderServiceImpl implements FolderService {

    @Override
    public FolderResponse create(CreateFolderRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public List<FolderResponse> getAll(Long parentId) {
        return Collections.emptyList();
    }

    @Override
    public FolderResponse rename(Long folderId, RenameRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public FolderResponse move(Long folderId, MoveRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(Long folderId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
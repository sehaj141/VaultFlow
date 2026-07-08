package com.vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
package com.vaultflow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageUsageResponse {
    private long usedBytes;
    private long limitBytes;
    private double percentageUsed;
}
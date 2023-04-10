package com.x00179223.librarybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChargeResponse {
    private String id;
    private Long amount;
    private String currency;
    private String status;

}

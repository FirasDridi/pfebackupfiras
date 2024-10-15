package com.mss.servicemanager.DTO;

import lombok.Data;
import java.util.UUID;

@Data
public class InvoiceUserDto {
    private UUID id;
    private String userName;
    private String emailId;
}

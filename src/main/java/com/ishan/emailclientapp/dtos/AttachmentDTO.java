package com.ishan.emailclientapp.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AttachmentDTO {
    private String fileName;
    private String fileType;
    private byte[] content;
}

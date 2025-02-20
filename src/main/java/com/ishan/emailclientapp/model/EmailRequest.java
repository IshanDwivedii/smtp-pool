package com.ishan.emailclientapp.model;
import java.util.*;

import com.ishan.emailclientapp.dtos.AttachmentDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {

    private String from;
    private String to;
    private List<String> cc;
    private List<String> bcc;
    private String subject;
    private String body;
    private boolean isHtml; //flag to indicate if body is html
    private List<AttachmentDTO> attachments;

}

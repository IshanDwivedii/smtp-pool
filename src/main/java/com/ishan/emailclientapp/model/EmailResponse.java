package com.ishan.emailclientapp.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class EmailResponse {
    private boolean success;
    private String message;
    private String messageId;

}

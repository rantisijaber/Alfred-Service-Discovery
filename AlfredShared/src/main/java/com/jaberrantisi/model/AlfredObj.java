package com.jaberrantisi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlfredObj {
    private AlfredMessageType messageType;
    private Object payload;

}

package jp.co.jun.edi.model;

import java.io.Serializable;

import jp.co.jun.edi.type.MessageCodeType;
import lombok.Data;

/**
 * MessageModel.
 */
@Data
public class MessageModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private MessageCodeType code;

    private String message;
}

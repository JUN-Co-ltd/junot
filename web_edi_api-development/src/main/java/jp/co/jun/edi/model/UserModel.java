package jp.co.jun.edi.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

/**
 * ユーザー.
 *
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserModel implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigInteger id;

    private String accountName;

    private List<String> authorities;

    private String company;

    private String companyName;

    private String name;

    private String mailAddress;
}

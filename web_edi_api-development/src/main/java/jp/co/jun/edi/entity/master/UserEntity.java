package jp.co.jun.edi.entity.master;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

/**
 * ユーザ情報のEntity.
 */
@Entity
@Data
public class UserEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** アカウント名. */
    @Column(name = "account_name")
    private String accountName;

    /** 有効/無効. */
    private boolean enabled;

    /** 権限. */
    private String authority;

    /** 所属会社. */
    private String company;

    /** 氏名. */
    private String name;

    /** メールアドレス. */
    @Column(name = "mail_address")
    private String mailAddress;

    /** メーカー名称. */
    @Column(name = "maker_name")
    private String makerName;
}

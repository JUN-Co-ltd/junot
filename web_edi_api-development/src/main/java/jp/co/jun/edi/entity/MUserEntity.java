package jp.co.jun.edi.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * ユーザマスタのEntity.
 */
@Entity
@Table(name = "m_user")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MUserEntity extends GenericDeletedAtUnixEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** アカウント名. */
    @Column(name = "account_name")
    private String accountName;

    /** パスワード. */
    private String password;

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

    /** システム管理. */
    @Column(name = "system_managed")
    private boolean systemManaged;
}

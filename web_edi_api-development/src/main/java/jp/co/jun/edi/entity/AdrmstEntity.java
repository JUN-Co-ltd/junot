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
 * タグデータメール送信先アドレスマスタのEntity.
 */
@Entity
//PRD_0184 mod JFE start
//@Table(name = "adrmst")
@Table(name = "m_adrmst")
//PRD_0184 mod JFE end
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class AdrmstEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** メールアドレス. */
    @Column(name = "email")
    private String email;

    /** 送信先名. */
    @Column(name = "name")
    private String name;

    /** 対象ブランド. */
    @Column(name = "brand01_60")
    private String brand01_60;

    /** 送信区分. */
    @Column(name = "ssnkbn")
    private String ssnkbn;

    /** 登録ユーザID. */
    @Column(name = "created_user_id")
    private BigInteger createdUserId;

    /** 更新ユーザID. */
    @Column(name = "updated_user_id")
    private BigInteger updatedUserId;
}

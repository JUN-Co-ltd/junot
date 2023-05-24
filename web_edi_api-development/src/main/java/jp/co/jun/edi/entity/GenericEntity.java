package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

import lombok.Data;

/**
 * 共通のEntity.
 */
@MappedSuperclass
@Data
public abstract class GenericEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 登録日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", insertable = false, updatable = false)
    private Date createdAt;

    /** 登録ユーザID. */
    @CreatedBy
    @Column(name = "created_user_id", updatable = false)
    private BigInteger createdUserId;

    /** 更新日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", insertable = false, updatable = false)
    private Date updatedAt;

    /** 更新ユーザID. */
    @LastModifiedBy
    @Column(name = "updated_user_id")
    private BigInteger updatedUserId;

    /** 削除日時. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private Date deletedAt;
}

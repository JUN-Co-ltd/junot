package jp.co.jun.edi.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.Data;

/**
 * TFileInfoSelectEntity.
 */
@Entity
//@Table(name = "t_file_info")
@EntityListeners(AuditingEntityListener.class)
@Data
public class TFileInfoSelectEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    @Column(name = "file_no_id")
    private BigInteger fileNoId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_category")
    private int fileCategory;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at")
    private Date createdAt;

    @Column(name = "created_user_id")
    private BigInteger createdUserId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at")
    private Date updatedAt;

    @Column(name = "updated_user_id")
    private BigInteger updatedUserId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "deleted_at")
    private Date deletedAt;
}

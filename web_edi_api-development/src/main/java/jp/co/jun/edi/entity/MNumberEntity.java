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
 * 採番マスタのEntity.
 */
@Entity
@Table(name = "m_number")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MNumberEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** テーブル名. */
    @Column(name = "table_name")
    private String tableName;

    /** カラム名. */
    @Column(name = "column_name")
    private String columnName;

    /** 現在値. */
    @Column(name = "now_number")
    private BigInteger nowNumber;

    /** 最小値. */
    @Column(name = "min_number")
    private BigInteger minNumber;

    /** 最大値. */
    @Column(name = "max_number")
    private BigInteger maxNumber;
}

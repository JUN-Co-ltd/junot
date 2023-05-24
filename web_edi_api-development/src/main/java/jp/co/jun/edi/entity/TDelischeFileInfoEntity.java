package jp.co.jun.edi.entity;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.DelischeFileStatusTypeConverter;
import jp.co.jun.edi.type.DelischeFileStatusType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * デリスケファイル情報のEntity.
 */
@Entity
@Table(name = "t_delische_file_info")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TDelischeFileInfoEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** ファイルID. */
    @Column(name = "file_no_id")
    private BigInteger fileNoId;

    /** ステータス. */
    @Convert(converter = DelischeFileStatusTypeConverter.class)
    private DelischeFileStatusType status;

    /** 検索条件. */
    @Column(name = "search_conditions")
    private String searchConditions;
}

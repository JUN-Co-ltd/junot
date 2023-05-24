package jp.co.jun.edi.entity.extended;

import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.GenericEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 拡張フクキタル品番情報のEntity.
 *
 */
@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedTFAttentionAppendicesTermEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;


    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 品番ID. */
    @Column(name = "part_no_id")
    private BigInteger partNoId;

    /** フクキタル品番ID. */
    @Column(name = "f_item_id")
    private BigInteger fItemId;

    /** カラーコード. */
    @Column(name = "color_code")
    private String colorCode;

    /** 付記用語ID. */
    @Column(name = "appendices_term_id")
    private BigInteger appendicesTermId;

    /** 付記用語コード. */
    @Column(name = "appendices_term_code")
    private String appendicesTermCode;

    /** 付記用語コード名. */
    @Column(name = "appendices_term_code_name")
    private String appendicesTermCodeName;

}

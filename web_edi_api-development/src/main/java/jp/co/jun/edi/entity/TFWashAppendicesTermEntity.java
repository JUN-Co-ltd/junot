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
 * The persistent class for the t_f_item database table.
 *
 */
@Entity
@Table(name = "t_f_wash_appendices_term")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TFWashAppendicesTermEntity extends GenericEntity {
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
}

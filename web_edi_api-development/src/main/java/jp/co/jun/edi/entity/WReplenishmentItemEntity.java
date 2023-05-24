package jp.co.jun.edi.entity;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Table;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.key.WReplenishmentItemKey;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 補充対象品番ワーク情報のEntity.
 */
@Entity
@Table(name = "w_replenishment_item")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class WReplenishmentItemEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** 複合主キー. */
    @EmbeddedId
    private WReplenishmentItemKey wReplenishmentItemKey;

    /** 品番. */
    @Column(name = "part_no")
    private String partNo;
}

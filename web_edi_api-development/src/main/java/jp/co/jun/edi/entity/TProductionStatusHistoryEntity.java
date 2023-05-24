package jp.co.jun.edi.entity;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jp.co.jun.edi.entity.converter.ProductionStatusTypeConverter;
import jp.co.jun.edi.type.ProductionStatusType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 生産ステータス履歴のEntity.
 */
@Entity
@Table(name = "t_production_status_history")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TProductionStatusHistoryEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** 発注ID. */
    @Column(name = "order_id")
    private BigInteger orderId;

    /** 発注No. */
    @Column(name = "production_status_id")
    private BigInteger productionStatusId;

    /** 生産ステータス. */
    @Column(name = "production_status_type")
    @Convert(converter = ProductionStatusTypeConverter.class)
    private ProductionStatusType productionStatusType;

    /** サンプル上がり予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sample_completion_at")
    private Date sampleCompletionAt;

    /** サンプル上がり確定予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sample_completion_fix_at")
    private Date sampleCompletionFixAt;

    /** 仕様確定予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "specification_at")
    private Date specificationAt;

    /** 仕様確定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "specification_fix_at")
    private Date specificationFixAt;

    /** 生地入荷予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "texture_arrival_at")
    private Date textureArrivalAt;

    /** 生地入荷確定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "texture_arrival_fix_at")
    private Date textureArrivalFixAt;

    /** 付属入荷予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "attachment_arrival_at")
    private Date attachmentArrivalAt;

    /** 付属入荷確定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "attachment_arrival_fix_at")
    private Date attachmentArrivalFixAt;

    /** 上がり予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completion_at")
    private Date completionAt;

    /** 上がり予定確定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "completion_fix_at")
    private Date completionFixAt;

    /** 上がり総数. */
    @Column(name = "completion_count")
    private Integer completionCount;

    /** 縫製検品到着予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sew_inspection_at")
    private Date sewInspectionAt;

    /** 縫製検品到着確定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "sew_inspection_fix_at")
    private Date sewInspectionFixAt;

    /** 検品実施予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "inspection_at")
    private Date inspectionAt;

    /** 検品実施確定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "inspection_fix_at")
    private Date inspectionFixAt;

    /** 出港予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "leave_port_at")
    private Date leavePortAt;

    /** 出港確定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "leave_port_fix_at")
    private Date leavePortFixAt;

    /** 入港予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "enter_port_at")
    private Date enterPortAt;

    /** 入港確定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "enter_port_fix_at")
    private Date enterPortFixAt;

    /** 通関予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "customs_clearance_at")
    private Date customsClearanceAt;

    /** 通関確定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "customs_clearance_fix_at")
    private Date customsClearanceFixAt;

    /** DISTA入荷予定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dista_arrival_at")
    private Date distaArrivalAt;

    /** DISTA入荷確定日. */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "dista_arrival_fix_at")
    private Date distaArrivalFixAt;

    /** メモ. */
    @Column(name = "memo")
    private String memo;
}

package jp.co.jun.edi.entity.extended.key;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import lombok.Data;

/**
 * 拡張補充出荷指示ワーク情報のKey.
 */
@Embeddable
@Data
public class ExtendedWReplenishmentShippingInstructionKey implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 管理情報日付. */
    @Temporal(TemporalType.DATE)
    @Column(name = "manage_date")
    private Date manageDate;

    /** 管理情報時間. */
    @Temporal(TemporalType.TIME)
    @Column(name = "manage_at")
    private Date manageAt;

    /** 管理情報 SEQ. */
    @Column(name = "sequence")
    private Integer sequence;
}

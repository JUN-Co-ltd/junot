package jp.co.jun.edi.entity.extended.key;

import java.io.Serializable;
import java.math.BigInteger;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import lombok.Data;

/**
 * 拡張入力補助セット情報のKey.
 */
@Embeddable
@Data
public class ExtendedMFInputAssistSetKey implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 拡張入力補助セットID. */
    @Column(name = "input_assist_set_id")
    private BigInteger inputAssistSetId;

    /** 拡張入力補助セット詳細ID. */
    @Column(name = "input_assist_set_detail_id")
    private BigInteger inputAssistSetDetailId;

}

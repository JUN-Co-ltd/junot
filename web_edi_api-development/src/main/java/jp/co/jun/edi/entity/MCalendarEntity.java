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

import jp.co.jun.edi.entity.converter.BusinessDayFlgTypeConverter;
import jp.co.jun.edi.entity.converter.CalendarTypeConverter;
import jp.co.jun.edi.type.BusinessDayFlgType;
import jp.co.jun.edi.type.CalendarType;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The persistent class for the m_calendar database table.
 *
 */
@Entity
@Table(name = "m_calendar")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class MCalendarEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** カレンダー種別. */
    @Convert(converter = CalendarTypeConverter.class)
    @Column(name = "calendar_type")
    private CalendarType calendarType;

    /** 日付. */
    @Temporal(TemporalType.DATE)
    @Column(name = "date")
    private Date date;

    /** 営業日. */
    @Convert(converter = BusinessDayFlgTypeConverter.class)
    @Column(name = "business_day_flg")
    private BusinessDayFlgType businessDayFlg;

    /** 高度な設定ID. */
    @Column(name = "advanced_setting_id")
    private BigInteger advancedSettingId;

}

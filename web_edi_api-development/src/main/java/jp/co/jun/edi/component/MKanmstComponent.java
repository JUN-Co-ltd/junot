package jp.co.jun.edi.component;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jp.co.jun.edi.entity.MKanmstEntity;
import jp.co.jun.edi.exception.ResourceNotFoundException;
import jp.co.jun.edi.message.ResultMessages;
import jp.co.jun.edi.repository.MKanmstRepository;
import jp.co.jun.edi.type.MessageCodeType;
import jp.co.jun.edi.util.DateUtils;
import jp.co.jun.edi.util.LogStringUtil;

/**
 * 管理マスタ関連のコンポーネント.
 */
@Component
public class MKanmstComponent extends GenericComponent {

    @Autowired
    private MKanmstRepository mKanmstRepository;

    private static final int PREVIOUS_MONTH_COUNT = -1;
    private static final int MONTH_BEFORE_END_COUNT = -2;
    //#PRD_0139_#10681 add JFE start
    /** 日付フォーマット：yyyyMMdd. */
    private static final String DATE_FORMAT_YYYY_MM_DD = "yyyyMMdd";
    //#PRD_0139_#10681 add JFE end
    /**
     * 管理マスタを取得する.
     *
     * @return 管理マスタ情報
     */
    public MKanmstEntity getMKanmstEntity() {
    MKanmstEntity mKanmstEntity = mKanmstRepository.findById(new BigInteger("1")).orElseThrow(
            () -> new ResourceNotFoundException(ResultMessages.warning().add(MessageCodeType.SYSTEM_ERROR)));


        return mKanmstEntity;
    }

    //#PRD_0139 #10681 add JFE start
    /**
     * 先頭の一件のみ管理マスタを取得する.
     *
     * @return 管理マスタ情報
     */
    public MKanmstEntity getTopMKanmstEntity() {
    MKanmstEntity mKanmstEntity = mKanmstRepository.findByTop().orElseThrow(
            () ->new ResourceNotFoundException(ResultMessages.warning().add(
                    MessageCodeType.CODE_002, LogStringUtil.of("ganerateInfo")
                    .message("Entity not found. m_kanmst")
                    .build())));

        return mKanmstEntity;
    }
    //#PRD_0139 #10681 add JFE end

    /**
     * 当月締日を取得する.
     *
     * @param simymd 締日
     * @return 締日
     */
    public Date getMonthEndAt(final String simymd) {
        return Date.from(comvertStringToLocalDate(simymd).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 前月締日を取得する.
     *
     * @param simymd 締日
     * @return 前月締日
     */
    public Date getPreviousMonthEndAt(final String simymd) {

        LocalDate ld = comvertStringToLocalDate(simymd).plusMonths(PREVIOUS_MONTH_COUNT);

        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 前々月締日を取得する.
     *
     * @param simymd 締日
     * @return 前月締日
     */
    public Date getMonthBeforeEndAt(final String simymd) {

        LocalDate ld = comvertStringToLocalDate(simymd).plusMonths(MONTH_BEFORE_END_COUNT);

        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 前々月締め日から前々々月締日を取得する.
     *
     * @param monthBeforeEndAt 前々月締め日
     * @return 前々々月締日
     */
    public Date getPreviousMonthBeforeEndAt(final String monthBeforeEndAt) {

        LocalDate ld = comvertStringToLocalDate(monthBeforeEndAt).plusMonths(PREVIOUS_MONTH_COUNT);

        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * String型の日付をDateに変換.
     *
     * @param strDate 日付
     * @return Date型に変換した日付
     */
    private LocalDate comvertStringToLocalDate(final String strDate) {
       return LocalDate.parse(strDate, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }

    //#PRD_0139 #10681 add JFE start
    /**
     * 更新用のエンティティを作成.
     *
     * @param userId 更新ユーザID
     * @return MKanmstEntity 更新管理マスタエンティティ
     */
    public MKanmstEntity generateMKanmstEntity(BigInteger userId) {
		 // 管理マスタ情報取得
       //final MKanmstEntity MKanmstEntity = getMKanmstEntity();
    	final MKanmstEntity MKanmstEntity = getTopMKanmstEntity();

       //今日の日付を取得する
       final Date DateNow = new Date(); //set用
       MKanmstEntity.setNitymd((DateUtils.formatFromDate(DateNow, DATE_FORMAT_YYYY_MM_DD)));
       MKanmstEntity.setUpdatedAt(DateNow);
       MKanmstEntity.setUpdatedUserId(userId);
       return MKanmstEntity;
	}
	//#PRD_0139 #10681 add JFE end

  //#PRD_0138 #10680 add JFE start
    /**
     * 日計日を取得する.
     *
     * @return Nitymd 日計日
     */
    public String getNitymd() {

    	String Nitymd = mKanmstRepository.getNitymd();

        return Nitymd;

    }

  //#PRD_0138 #10680 add JFE end
}

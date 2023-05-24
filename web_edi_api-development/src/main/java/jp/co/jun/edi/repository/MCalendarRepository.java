package jp.co.jun.edi.repository;

import java.math.BigInteger;
import java.util.Date;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jp.co.jun.edi.entity.MCalendarEntity;
import jp.co.jun.edi.type.CalendarType;

/**
 * カレンダマスタを検索するリポジトリ.
 */
public interface MCalendarRepository
        extends JpaRepository<MCalendarEntity, BigInteger>, JpaSpecificationExecutor<MCalendarEntity> {
    /**
     * カレンダ種別と期間を指定してカレンダー情報を取得する.
     * @param calendarType カレンダ種別
     * @param startAt 開始日
     * @param endAt 終了日
     * @param pageable {@link Pageable} instance
     * @return カレンダ情報
     */
    @Query("SELECT t FROM MCalendarEntity t"
            + " WHERE t.calendarType = :calendarType"
            + " AND t.date >= :startAt"
            + " AND t.date <= :endAt"
            + " AND t.deletedAt IS NULL")
    Page<MCalendarEntity> findByCalendarTypeAndSpecifyDateRange(
            @Param("calendarType") CalendarType calendarType,
            @Param("startAt") Date startAt,
            @Param("endAt") Date endAt,
            Pageable pageable);

    /**
     * カレンダ種別と日付を指定してカレンダー情報を取得する.
     * @param calendarType カレンダ種別
     * @param date 日付
     * @return カレンダ情報
     */
    @Query("SELECT t FROM MCalendarEntity t"
            + " WHERE t.calendarType = :calendarType"
            + " AND t.date = :date"
            + " AND t.deletedAt IS NULL")
    Optional<MCalendarEntity> findByCalendarTypeAndDate(
            @Param("calendarType") CalendarType calendarType,
            @Param("date") Date date);
}

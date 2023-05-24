package jp.co.jun.edi.entity;

import java.math.BigInteger;
import java.util.Date;

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
 * お知らせ情報のEntity.
 */
@Entity
@Table(name = "t_news")
@EntityListeners(AuditingEntityListener.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class TNewsEntity extends GenericEntity {
    private static final long serialVersionUID = 1L;

    /** ID. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private BigInteger id;

    /** タイトル. */
    @Column(name = "title")
    private String title;

    /** 本文. */
    @Column(name = "content")
    private String content;

    /** 公開開始日時. */
    @Column(name = "open_start_at")
    private Date openStartAt;

    /** 公開終了日時. */
    @Column(name = "open_end_at")
    private Date openEndAt;

    /** 新着表示終了日時. */
    @Column(name = "new_display_end_at")
    private Date newDisplayEndAt;
}

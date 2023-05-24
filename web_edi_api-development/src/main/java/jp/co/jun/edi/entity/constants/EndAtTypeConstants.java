package jp.co.jun.edi.entity.constants;

/**
 * 締め日種類のCONSTクラス.
 */
public final class EndAtTypeConstants {
    /** 当月. */
    public static final int THIS_MONTH = 1;

    /** 前月. */
    public static final int PREVIOUS_MONTH = 2;

    /** 前々月. */
    public static final int MONTH_BEFORE = 3;

    /** 前々月以前. */
    public static final int PAST_MONTH = 0;

    /** 未来月. */
    public static final int FUTURE_MONTH = 9;

    /**
     */
    private EndAtTypeConstants() {
    };
}

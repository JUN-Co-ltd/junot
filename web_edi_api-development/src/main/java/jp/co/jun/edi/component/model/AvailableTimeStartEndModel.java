package jp.co.jun.edi.component.model;

import lombok.Data;

/**
 * JUNoT利用可能時間のModel.
 */
@Data
public class AvailableTimeStartEndModel {
    /** 開始時刻(HHmmmss). */
    private final int startTime;

    /** 終了時刻(HHmmmss). */
    private final int endTime;
}

import * as Moment_ from 'moment';
import { Injectable } from '@angular/core';
import { NgbDateParserFormatter } from '@ng-bootstrap/ng-bootstrap';

import { DelischeSearchType } from '../const/const';

import { StringUtils } from '../util/string-utils';
import { NumberUtils } from '../util/number-utils';
import { FormUtils } from '../util/form-utils';

import { DelischeOrderSearchConditions } from '../model/delische-order-search-conditions';

const Moment = Moment_;

/**
 * デリスケに関するService.
 */
@Injectable({
  providedIn: 'root'
})
export class DelischeService {

  constructor(
    private ngbDateParserFormatter: NgbDateParserFormatter
  ) { }

  /**
   * デリスケ発注検索のデータ整形処理
   * @param searchConditions デリスケ発注検索条件
   * @returns 整形後検索条件
   */
  convertDelischeOrderRequestData(searchConditions: DelischeOrderSearchConditions): DelischeOrderSearchConditions {
    const copyItem = JSON.parse(JSON.stringify(searchConditions));
    // 品番はハイフン除去して検索
    const partNo = copyItem.partNo;
    if (StringUtils.isNotEmpty(partNo)) {
      copyItem.partNo = partNo.replace('-', '');
    }

    const searchSelect = copyItem.searchSelect;
    switch (searchSelect) {
      case DelischeSearchType.ORDER:
        // 発注納期from
        if (copyItem.productDeliveryAtFrom && typeof copyItem.productDeliveryAtFrom !== 'string') {
          copyItem.productDeliveryAtFrom = this.ngbDateParserFormatter.format(copyItem.productDeliveryAtFrom).replace(/-/g, '/');
        }
        // 発注納期to
        if (copyItem.productDeliveryAtTo && typeof copyItem.productDeliveryAtTo !== 'string') {
          copyItem.productDeliveryAtTo = this.ngbDateParserFormatter.format(copyItem.productDeliveryAtTo).replace(/-/g, '/');
        }
        break;

      case DelischeSearchType.DERIVERY:
        // 納品日from
        if (copyItem.deliveryAtFrom && typeof copyItem.deliveryAtFrom !== 'string') {
          copyItem.deliveryAtFrom = this.ngbDateParserFormatter.format(copyItem.deliveryAtFrom).replace(/-/g, '/');
        }
        // 納品日to
        if (copyItem.deliveryAtTo && typeof copyItem.deliveryAtTo !== 'string') {
          copyItem.deliveryAtTo = this.ngbDateParserFormatter.format(copyItem.deliveryAtTo).replace(/-/g, '/');
        }
        break;
      default:
    }

    return copyItem;
  }

  /**
   * 年度・月度から生産納期fromを算出する.
   * 年が未入力ならnullを返す
   * 月度が未入力なら前年の12月21日を返す
   * 月度が入力されていれば前月の21日を返す
   * @param searchConditions 検索条件
   * @return 生産納期from
   */
  generateProductDeliveryAtDateFrom(searchConditions: DelischeOrderSearchConditions): string {
    const productDeliveryAtMonthlyYearFrom = searchConditions.productDeliveryAtMonthlyYearFrom;
    const productDeliveryAtMonthlyFrom = searchConditions.productDeliveryAtMonthlyFrom;

    if (!NumberUtils.isNumber(productDeliveryAtMonthlyYearFrom)) { return null; }

    let year = NumberUtils.toInteger(productDeliveryAtMonthlyYearFrom);
    if (!NumberUtils.isNumber(productDeliveryAtMonthlyFrom)) {
      const defaultDate = new Date(year - 1, 11, 21);
      return Moment(defaultDate).format('YYYY/MM/DD');
    }

    let month = productDeliveryAtMonthlyFrom - 1;
    if (productDeliveryAtMonthlyFrom === 1) {
        year = productDeliveryAtMonthlyYearFrom - 1;
        month = 12;
    }

    const date = new Date(year, (month - 1), 21);
    return Moment(date).format('YYYY/MM/DD');
  }

  /**
   * 年度・月度から生産納期toを算出する.
   * 年が未入力ならnullを返す
   * 月度が未入力なら12月20日を返す
   * 月度が入力されていれば20日を返す
   * @param searchConditions 検索条件
   * @return 生産納期to
   */
  generateProductDeliveryAtDateTo(searchConditions: DelischeOrderSearchConditions): string {
    const productDeliveryAtMonthlyYearTo = searchConditions.productDeliveryAtMonthlyYearTo;
    const productDeliveryAtMonthlyTo = searchConditions.productDeliveryAtMonthlyTo;

    if (!NumberUtils.isNumber(productDeliveryAtMonthlyYearTo)) { return null; }
    const month = NumberUtils.isNumber(productDeliveryAtMonthlyTo) ? productDeliveryAtMonthlyTo : 12;
    const date = new Date(NumberUtils.toInteger(productDeliveryAtMonthlyYearTo), (month - 1), 20);
    return Moment(date).format('YYYY/MM/DD');
  }

  /**
   * 年度、納品週から納品日fromを算出する.
   * @param searchConditions 検索条件
   * @returns 納品日from
   */
  generateDeliveryAtDateFrom(searchConditions: DelischeOrderSearchConditions): string {
    const mdWeekYearFrom = searchConditions.mdWeekYearFrom;
    let mdWeekFrom = searchConditions.mdWeekFrom;

    if (FormUtils.isEmpty(mdWeekYearFrom)) {
      return null;
    }

    if (FormUtils.isEmpty(mdWeekFrom)) {
      // 週番号未入力の場合、1週扱い
      mdWeekFrom = 1;
    }

    // 月曜日を返す
    return Moment(mdWeekYearFrom, 'YYYY/MM/DD', 'ja').add(mdWeekFrom - 1, 'weeks').startOf('isoWeek').format('YYYY/MM/DD');
  }

  /**
   * 年度、納品週から納品日toを算出する.
   * @param searchConditions 検索条件
   * @returns 納品日to
   */
  generateDeliveryAtDateTo(searchConditions: DelischeOrderSearchConditions): string {
    const mdWeekYearTo = searchConditions.mdWeekYearTo;
    let mdWeekTo = searchConditions.mdWeekTo;

    if (FormUtils.isEmpty(mdWeekYearTo)) {
      return null;
    }

    if (FormUtils.isEmpty(mdWeekTo)) {
      // 週番号未入力の場合、最終週扱い
      if (Moment(mdWeekYearTo, 'YYYY/MM/DD', 'ja').endOf('year').weekday() === 0) {
        mdWeekTo = 53;  // 年度末が日曜の場合は最終週は53
      } else {
        mdWeekTo = 52;
      }
    }

    // 日曜日を設定
    return Moment(mdWeekYearTo, 'YYYY/MM/DD', 'ja').add(mdWeekTo - 1, 'weeks').endOf('isoWeek').format('YYYY/MM/DD');
  }
}

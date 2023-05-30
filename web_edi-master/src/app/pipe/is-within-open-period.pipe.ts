import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'isWithinOpenPeriod'
})
export class IsWithinOpenPeriodPipe implements PipeTransform {

  /**
   * 公開期間かチェックする。
   * @param value {news(お知らせ情報),today(当日日時)}
   * @return 公開期間内:true/期間外:false
   */
  transform(value: any, args?: any): boolean {
    if (value.news === null || value.news.openStartAt === null || value.news.openStartAt === undefined) {
      return false;
    }

    const today = (value.today === null || value.today === undefined) ? new Date() : value.today;

    if (value.news.openEndAt === null || value.news.openEndAt === undefined) {
      // 公開終了日がnullであれば開始日のみチェック
      return value.news.openStartAt <= today;
    } else {
      return value.news.openStartAt <= today && today < value.news.openEndAt;
    }
  }
}

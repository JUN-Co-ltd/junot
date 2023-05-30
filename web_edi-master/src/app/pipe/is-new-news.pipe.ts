import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'isNewNews'
})
export class IsNewNewsPipe implements PipeTransform {

  /**
   * 新着のお知らせかチェックする。
   * @param value {news(お知らせ情報),today(当日日時)}
   * @return 新着:true/新着ではない:false
   */
  transform(value: any, args: any): boolean {
    if (value.news === null || value.news.newDisplayEndAt === null || value.news.newDisplayEndAt === undefined) {
      return false;
    }

    const today = (value.today === null || value.today === undefined) ? new Date() : value.today;

    return today <= value.news.newDisplayEndAt;
  }
}

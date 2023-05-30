import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'maintNewsStatus'
})
export class MaintNewsStatusPipe implements PipeTransform {
  /**
   * 公開開始日時、公開終了日時、現在日時を比較し、
   * 結果として、以下のステータスを返す
   *  - 公開前 : 公開開始日時 > 現在日時
   *  - 公開中 : 公開開始日時 <= 現在日時 <= 公開終了日時(公開終了日時がないときは比較しない)
   *  - 公開終了 : 公開開始日時 <= 現在日時 > 公開終了日時(公開終了日時がないときは比較しない)
   * @param openStartAt 公開開始日時
   * @param openEndAt   公開終了日時
   */
  transform(openStartAt: Date, openEndAt: Date): string {
    const nowTime = new Date().getTime();

    if (openStartAt.getTime() > nowTime) {
      return '公開前';
    }

    if (openEndAt === null || openEndAt === undefined) {
      return '公開中';
    }

    if (nowTime <= openEndAt.getTime()) {
      return '公開中';
    } else {
      return '公開終了';
    }
  }
}

import { Pipe, PipeTransform } from '@angular/core';

@Pipe({
  name: 'deliveryCount'
})
export class DeliveryCountPipe implements PipeTransform {

  /**
   * 2桁のゼロパディングで返す.
   * 0の場合は「未承認」を返す.
   * @param deliveryCount 納品依頼回数
   */
  transform(deliveryCount: number): string {
    if (deliveryCount === 0) {
      return '未承認';
    }
    return ('00' + deliveryCount).slice(-2);  // 0桁ゼロパディング
  }
}

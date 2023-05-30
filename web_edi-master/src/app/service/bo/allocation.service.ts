import { Injectable } from '@angular/core';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';

@Injectable({
  providedIn: 'root'
})
export class AllocationService {

  constructor() { }

  /**
   * @param allocations 配分課リスト
   * @param divisionCode 配分課コード
   * @returns 指定した配分課コードの配分課名
   */
  findAllocationName(allocations: JunpcCodmst[], divisionCode: string): string | null {
    const target = allocations.find(allocation => {
      const code1 = allocation.code1;
      const substr = code1.substring(code1.length - 2, code1.length);
      return divisionCode === substr;
    });

    return target == null ? null : target.item1;
  }
}

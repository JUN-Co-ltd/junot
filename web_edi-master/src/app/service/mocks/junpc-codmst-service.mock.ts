import { GenericList } from 'src/app/model/generic-list';
import { JunpcCodmst } from 'src/app/model/junpc-codmst';
import { of, Observable } from 'rxjs';

const list = [
  { code1: '11', item1: '東京１課' },
  { code1: '12', item1: '東京２課' },
  { code1: '13', item1: '東京３課' },
  { code1: '14', item1: '東京４課' },
  { code1: '15', item1: '東京５課' },
  { code1: '16', item1: '東京６課' },
  { code1: '17', item1: '東京７課' },
  { code1: '18', item1: '縫製検品' },
  { code1: '21', item1: '関西１課' },
  { code1: '22', item1: '中国１課' }
];

export function fetchDistributionSectionsMock(searchCondition): Observable<GenericList<JunpcCodmst>> {
  return of({
    items: [list.find(l => l.item1 === searchCondition.divisionCode) as JunpcCodmst],
    nextPageToken: 'aaaaa'
  } as GenericList<JunpcCodmst>);
}

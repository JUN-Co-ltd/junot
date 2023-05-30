export class CodeMaster {
  // サブシーズンの値を定義する
  public static readonly subSeason: { id: number, value: string }[] = [
    { id: 1, value: 'A1' },
    { id: 2, value: 'A2' },
    { id: 5, value: 'B1' },
    { id: 6, value: 'B2' },
    { id: 9, value: 'C' }
  ];
  // シーズンコードを元に画面に表示するシーズン名を定義する
  public static readonly seasonName: { code: string, value: string }[] = [
    { code: 'A', value: '春夏' },
    { code: 'B', value: '秋冬' },
    { code: 'C', value: '年間' },
  ];

  // 加算日の値を定義する
  public static readonly addDays: { brandCode: string, itemCode: string, addDay: number }[] = [
    { brandCode: null, itemCode: null, addDay: 28 } // デフォルト
  ];

}

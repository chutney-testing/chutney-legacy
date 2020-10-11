import { Directive, ElementRef, Input, OnInit, OnDestroy } from '@angular/core';
import { Router, ActivationEnd } from '@angular/router';
import { filter } from 'rxjs/operators';

@Directive({
  selector: '[appBanner]',
})
export class AppBannerDirective implements OnInit, OnDestroy {
  colorPlate = [
    '#FFEBEE',
    '#FFCDD2',
    '#EF9A9A',
    '#E57373',
    '#EF5350',
    '#F44336',
    '#E53935',
    '#D32F2F',
    '#C62828',
    '#B71C1C',
    '#FF8A80',
    '#FF5252',
    '#FF1744',
    '#D50000',
    '#FCE4EC',
    '#F8BBD0',
    '#F48FB1',
    '#F06292',
    '#EC407A',
    '#E91E63',
    '#D81B60',
    '#C2185B',
    '#AD1457',
    '#880E4F',
    '#FF80AB',
    '#FF4081',
    '#F50057',
    '#C51162',
    '#F3E5F5',
    '#E1BEE7',
    '#CE93D8',
    '#BA68C8',
    '#AB47BC',
    '#9C27B0',
    '#8E24AA',
    '#7B1FA2',
    '#6A1B9A',
    '#4A148C',
    '#EA80FC',
    '#E040FB',
    '#D500F9',
    '#AA00FF',
    '#EDE7F6',
    '#D1C4E9',
    '#B39DDB',
    '#9575CD',
    '#7E57C2',
    '#673AB7',
    '#5E35B1',
    '#512DA8',
    '#4527A0',
    '#311B92',
    '#B388FF',
    '#7C4DFF',
    '#651FFF',
    '#6200EA',
    '#E8EAF6',
    '#C5CAE9',
    '#9FA8DA',
    '#7986CB',
    '#5C6BC0',
    '#3F51B5',
    '#3949AB',
    '#303F9F',
    '#283593',
    '#1A237E',
    '#8C9EFF',
    '#536DFE',
    '#3D5AFE',
    '#304FFE',
    '#E3F2FD',
    '#BBDEFB',
    '#90CAF9',
    '#64B5F6',
    '#42A5F5',
    '#2196F3',
    '#1E88E5',
    '#1976D2',
    '#1565C0',
    '#0D47A1',
    '#82B1FF',
    '#448AFF',
    '#2979FF',
    '#2962FF',
    '#E1F5FE',
    '#B3E5FC',
    '#81D4FA',
    '#4FC3F7',
    '#29B6F6',
    '#03A9F4',
    '#039BE5',
    '#0288D1',
    '#0277BD',
    '#01579B',
    '#80D8FF',
    '#40C4FF',
    '#00B0FF',
    '#0091EA',
    '#E0F2F1',
    '#B2DFDB',
    '#80CBC4',
    '#4DB6AC',
    '#26A69A',
    '#009688',
    '#00897B',
    '#00796B',
    '#00695C',
    '#004D40',
    '#A7FFEB',
    '#64FFDA',
    '#1DE9B6',
    '#00BFA5',
    '#E0F7FA',
    '#B2EBF2',
    '#80DEEA',
    '#4DD0E1',
    '#26C6DA',
    '#00BCD4',
    '#00ACC1',
    '#0097A7',
    '#00838F',
    '#006064',
    '#84FFFF',
    '#18FFFF',
    '#00E5FF',
    '#00B8D4',
    '#E8F5E9',
    '#C8E6C9',
    '#A5D6A7',
    '#81C784',
    '#66BB6A',
    '#4CAF50',
    '#43A047',
    '#388E3C',
    '#2E7D32',
    '#1B5E20',
    '#B9F6CA',
    '#69F0AE',
    '#00E676',
    '#00C853',
    '#F1F8E9',
    '#DCEDC8',
    '#C5E1A5',
    '#AED581',
    '#9CCC65',
    '#8BC34A',
    '#7CB342',
    '#689F38',
    '#558B2F',
    '#33691E',
    '#CCFF90',
    '#B2FF59',
    '#76FF03',
    '#64DD17',
    '#F9FBE7',
    '#F0F4C3',
    '#E6EE9C',
    '#DCE775',
    '#D4E157',
    '#CDDC39',
    '#C0CA33',
    '#AFB42B',
    '#9E9D24',
    '#827717',
    '#F4FF81',
    '#EEFF41',
    '#C6FF00',
    '#AEEA00',
    '#FFFDE7',
    '#FFF9C4',
    '#FFF59D',
    '#FFF176',
    '#FFEE58',
    '#FFEB3B',
    '#FDD835',
    '#FBC02D',
    '#F9A825',
    '#F57F17',
    '#FFFF8D',
    '#FFFF00',
    '#FFEA00',
    '#FFD600',
    '#FFF8E1',
    '#FFECB3',
    '#FFE082',
    '#FFD54F',
    '#FFCA28',
    '#FFC107',
    '#FFB300',
    '#FFA000',
    '#FF8F00',
    '#FF6F00',
    '#FFE57F',
    '#FFD740',
    '#FFC400',
    '#FFAB00',
    '#FFF3E0',
    '#FFE0B2',
    '#FFCC80',
    '#FFB74D',
    '#FFA726',
    '#FF9800',
    '#FB8C00',
    '#F57C00',
    '#EF6C00',
    '#E65100',
    '#FFD180',
    '#FFAB40',
    '#FF9100',
    '#FF6D00',
    '#FBE9E7',
    '#FFCCBC',
    '#FFAB91',
    '#FF8A65',
    '#FF7043',
    '#FF5722',
    '#F4511E',
    '#E64A19',
    '#D84315',
    '#BF360C',
    '#FF9E80',
    '#FF6E40',
    '#FF3D00',
    '#DD2C00',
    '#FAFAFA',
    '#F5F5F5',
    '#EEEEEE',
    '#E0E0E0',
    '#BDBDBD',
    '#9E9E9E',
    '#757575',
    '#616161',
    '#424242',
    '#212121',
    '#ECEFF1',
    '#CFD8DC',
    '#B0BEC5',
    '#90A4AE',
    '#78909C',
    '#607D8B',
    '#546E7A',
    '#455A64',
    '#37474F',
    '#263238',
    '#EFEBE9',
    '#D7CCC8',
    '#BCAAA4',
    '#A1887F',
    '#8D6E63',
    '#795548',
    '#6D4C41',
    '#5D4037',
    '#4E342E',
    '#3E2723',
  ];

  canvas = null;
  ctx = null;
  width = 0;
  height = 0;

  @Input() imageType = 'jpeg';
  @Input() quality = 1;

  constructor(private router: Router, private element: ElementRef) {
    // 监听路由变化
    this.router.events
      .pipe(
        filter((event) => event instanceof ActivationEnd),
        filter((x) => !!this.canvas)
      )
      .subscribe((route: ActivationEnd) => this.change());
  }

  ngOnInit() {
    this.width = this.element.nativeElement.clientWidth;
    this.height = this.element.nativeElement.clientHeight;
    this.canvas = this.createCanvas(this.width, this.height);
    this.element.nativeElement.style.cssText += `
            background-repeat: no-repeat;
            background-size: cover;`;
    this.change();
  }

  ngOnDestroy() {
    const cssText = this.element.nativeElement.style.cssText;
    this.element.nativeElement.style.cssText = cssText.replace(
      /background[^;]+;/g,
      ''
    );
  }

  change() {
    const count = this.random(0, 8) + 6;
    const [background, ...colors] = this.getColors(count);
    const width = this.width;
    const height = this.height;
    this.ctx = this.canvas.getContext('2d');

    this.fill(background, width, height);

    let i = 0;
    while (i < count) {
      this.draw(this.getShape(), {
        color: colors[i],
        width,
        height,
      });
      i += 1;
    }
    this.output();
  }

  random(min: number, max: number) {
    return Math.floor(Math.random() * (max - min + 1) + min);
  }

  getRandomColor() {
    return this.colorPlate[this.random(0, this.colorPlate.length - 1)];
  }

  getColors(count: number) {
    const colors = [];
    for (let i = 0; i <= count; ) {
      const color = this.getRandomColor();
      if (colors.indexOf(color) === -1) {
        colors.push(color);
        i += 1;
      }
    }
    return colors;
  }

  createCanvas(width: number, height: number) {
    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;
    return canvas;
  }

  getShape() {
    return ['rect', 'arc'][this.random(0, 1)];
  }

  output() {
    const dataUrl = this.toDataUrl(this.imageType, this.quality);
    this.element.nativeElement.style.backgroundImage = `url("${dataUrl}")`;
  }

  toDataUrl(imageType = 'jpeg', quality = 1) {
    return this.canvas.toDataURL(`image/${imageType}`, quality);
  }

  shadow({ blur, x = 1, y = 1, color = 'rgba(0, 0, 0, .3)' }) {
    this.ctx.shadowBlur = blur;
    this.ctx.shadowOffsetX = x;
    this.ctx.shadowOffsetY = y;
    this.ctx.shadowColor = color;
  }

  rect({ width, height }) {
    const r = (this.random(10, 360) * Math.PI) / 180;
    const w =
      this.random(100, Math.max(width, height)) * (this.random(-10, 10) || 1);
    const h =
      this.random(100, Math.min(width, height)) * (this.random(-10, 10) || 1);
    this.ctx.rotate(r);
    this.ctx.rect(0, 0, w, h);
    this.ctx.rotate(-r);
  }

  arc({ width, height }) {
    const r = this.random(100, Math.min(width, height) / 2);
    this.ctx.arc(0, 0, r, 0, 2 * Math.PI);
  }

  draw(sharp: string, option: any) {
    const { width, height, color } = option;

    this.ctx.beginPath();
    this.shadow({
      blur: 20,
    });

    const x = this.random(0, width);
    const y = this.random(0, height);
    this.ctx.translate(x, y);
    this[sharp](option);
    this.ctx.closePath();
    this.ctx.fillStyle = color;
    this.ctx.fill();
    this.ctx.translate(-x, -y);
  }

  fill(color: string, width: number, height: number) {
    this.ctx.fillStyle = color;
    this.ctx.fillRect(0, 0, width, height);
  }
}

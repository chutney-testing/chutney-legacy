export default class AsciidocConverter {
  convert(content: string): string;

  styleEmbeddedDocWithLeftToc(baseAsciiDocElement: Element, styleClass: string);

  isElementFromToc(baseAsciiDocElement: Element, element: Element);
}

"use strict";

class AsciidocConverter {

  constructor() {
    this.asciidoctor = Asciidoctor();
    this.TOC_CLASSNAME = 'toc';
    this.options = {
      safe: 'safe',
      header_footer: false,
      attributes: {
        showtitle: true,
        icons: 'font',
        toc: 'auto'
      }
    };
  }

  convert(content) {
    var htmlContent = this.asciidoctor.convert(content, this.options);
    return htmlContent;
  }

  styleEmbeddedDocWithLeftToc(baseAsciiDocElement, styleClass) {
    if (baseAsciiDocElement.querySelector('.'+this.TOC_CLASSNAME) != null) {
      const allChildren = baseAsciiDocElement.children;
      for (var i = 0; i < allChildren.length; i++) {
        if (!allChildren[i].classList.contains(this.TOC_CLASSNAME)) {
          allChildren[i].classList.add(styleClass);
        }
      }
    }
  }

  isElementFromToc(baseAsciiDocElement, element) {
    const toc = baseAsciiDocElement.querySelector('.'+this.TOC_CLASSNAME);
    if (toc !== null) {
      let node = element.parentNode;
      while (node !== null) {
        if (node === toc) {
          return true;
        }
        node = node.parentNode;
      }
      return false;
    }
    return false;
  }

}

export default AsciidocConverter;

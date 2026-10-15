/**
 * AquaFlow - Complete System Documentation Generator
 * Generates an exhaustive, production-grade Microsoft Word (.docx) document
 * covering the entire architecture, database schema, user workflows, API endpoints,
 * calculation engines, security mechanisms, and UI specifications.
 */

const fs = require('fs');
const path = require('path');
const {
  Document,
  Packer,
  Paragraph,
  TextRun,
  Table,
  TableRow,
  TableCell,
  HeadingLevel,
  AlignmentType,
  BorderStyle,
  WidthType,
  ShadingType
} = require('docx');

// --- COLOR PALETTE & STYLES ---
const COLORS = {
  PRIMARY_NAVY: '003554',
  PRIMARY_TEAL: '005F73',
  ACCENT_CYAN: '0A9396',
  DARK_GRAY: '1E293B',
  MUTED_TEXT: '64748B',
  LIGHT_BG: 'F8FAFC',
  TABLE_HEADER_BG: '003554',
  TABLE_ALT_BG: 'F1F5F9',
  BORDER_COLOR: 'CBD5E1',
  SUCCESS_GREEN: '166534',
  SUCCESS_BG: 'F0FDF4',
  WARNING_ORANGE: '9A3412',
  WARNING_BG: 'FFFBEB',
  ALERT_RED: '991B1B',
  ALERT_BG: 'FEF2F2',
  WHITE: 'FFFFFF'
};

const BORDER_STYLE = {
  style: BorderStyle.SINGLE,
  size: 1,
  color: COLORS.BORDER_COLOR
};

const CELL_MARGINS = {
  top: 120,
  bottom: 120,
  left: 150,
  right: 150
};

// --- HELPER BUILDERS ---

function pTitle(text) {
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 400, after: 150 },
    children: [
      new TextRun({
        text: text,
        bold: true,
        size: 48,
        color: COLORS.PRIMARY_NAVY,
        font: 'Arial'
      })
    ]
  });
}

function pSubtitle(text) {
  return new Paragraph({
    alignment: AlignmentType.CENTER,
    spacing: { before: 0, after: 400 },
    children: [
      new TextRun({
        text: text,
        size: 24,
        color: COLORS.PRIMARY_TEAL,
        font: 'Arial'
      })
    ]
  });
}

function h1(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_1,
    spacing: { before: 400, after: 160 },
    children: [
      new TextRun({
        text: text,
        bold: true,
        size: 32,
        color: COLORS.PRIMARY_NAVY,
        font: 'Arial'
      })
    ]
  });
}

function h2(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_2,
    spacing: { before: 300, after: 120 },
    children: [
      new TextRun({
        text: text,
        bold: true,
        size: 26,
        color: COLORS.PRIMARY_TEAL,
        font: 'Arial'
      })
    ]
  });
}

function h3(text) {
  return new Paragraph({
    heading: HeadingLevel.HEADING_3,
    spacing: { before: 200, after: 80 },
    children: [
      new TextRun({
        text: text,
        bold: true,
        size: 22,
        color: COLORS.ACCENT_CYAN,
        font: 'Arial'
      })
    ]
  });
}

function p(text, options = {}) {
  return new Paragraph({
    spacing: { before: 80, after: 80 },
    alignment: options.alignment || AlignmentType.LEFT,
    children: [
      new TextRun({
        text: text,
        size: 20,
        color: options.color || COLORS.DARK_GRAY,
        font: 'Arial',
        bold: options.bold || false,
        italics: options.italics || false
      })
    ]
  });
}

function bullet(text, boldPrefix = '') {
  const children = [];
  if (boldPrefix) {
    children.push(new TextRun({
      text: boldPrefix + ' ',
      bold: true,
      size: 20,
      color: COLORS.DARK_GRAY,
      font: 'Arial'
    }));
  }
  children.push(new TextRun({
    text: text,
    size: 20,
    color: COLORS.DARK_GRAY,
    font: 'Arial'
  }));

  return new Paragraph({
    bullet: { level: 0 },
    spacing: { before: 40, after: 40 },
    children: children
  });
}

function callout(title, message, type = 'info') {
  let bgColor = COLORS.LIGHT_BG;
  let textColor = COLORS.PRIMARY_NAVY;
  let borderColor = COLORS.PRIMARY_TEAL;

  if (type === 'success') {
    bgColor = COLORS.SUCCESS_BG;
    textColor = COLORS.SUCCESS_GREEN;
    borderColor = '86EFAC';
  } else if (type === 'warning') {
    bgColor = COLORS.WARNING_BG;
    textColor = COLORS.WARNING_ORANGE;
    borderColor = 'FDE68A';
  } else if (type === 'danger') {
    bgColor = COLORS.ALERT_BG;
    textColor = COLORS.ALERT_RED;
    borderColor = 'FCA5A5';
  }

  return new Table({
    width: { size: 100, type: WidthType.PERCENTAGE },
    rows: [
      new TableRow({
        children: [
          new TableCell({
            margins: { top: 160, bottom: 160, left: 200, right: 200 },
            shading: { fill: bgColor, type: ShadingType.CLEAR },
            borders: {
              left: { style: BorderStyle.SINGLE, size: 24, color: borderColor },
              top: { style: BorderStyle.NONE },
              right: { style: BorderStyle.NONE },
              bottom: { style: BorderStyle.NONE }
            },
            children: [
              new Paragraph({
                spacing: { before: 0, after: 60 },
                children: [
                  new TextRun({
                    text: title,
                    bold: true,
                    size: 20,
                    color: textColor,
                    font: 'Arial'
                  })
                ]
              }),
              new Paragraph({
                spacing: { before: 0, after: 0 },
                children: [
                  new TextRun({
                    text: message,
                    size: 19,
                    color: COLORS.DARK_GRAY,
                    font: 'Arial'
                  })
                ]
              })
            ]
          })
        ]
      })
    ]
  });
}

function codeBlock(codeLines) {
  return new Table({
    width: { size: 100, type: WidthType.PERCENTAGE },
    rows: [
      new TableRow({
        children: [
          new TableCell({
            margins: { top: 140, bottom: 140, left: 160, right: 160 },
            shading: { fill: COLORS.TABLE_ALT_BG, type: ShadingType.CLEAR },
            borders: {
              top: BORDER_STYLE,
              bottom: BORDER_STYLE,
              left: BORDER_STYLE,
              right: BORDER_STYLE
            },
            children: codeLines.map(line => new Paragraph({
              spacing: { before: 20, after: 20 },
              children: [
                new TextRun({
                  text: line,
                  size: 18,
                  color: '0F172A',
                  font: 'Consolas'
                })
              ]
            }))
          })
        ]
      })
    ]
  });
}

function createTable(headers, rows, colWidths = []) {
  const headerCells = headers.map((h, i) => {
    const cellConfig = {
      margins: CELL_MARGINS,
      shading: { fill: COLORS.TABLE_HEADER_BG, type: ShadingType.CLEAR },
      borders: {
        top: BORDER_STYLE,
        bottom: BORDER_STYLE,
        left: BORDER_STYLE,
        right: BORDER_STYLE
      },
      children: [
        new Paragraph({
          spacing: { before: 40, after: 40 },
          children: [
            new TextRun({
              text: h,
              bold: true,
              size: 19,
              color: COLORS.WHITE,
              font: 'Arial'
            })
          ]
        })
      ]
    };
    if (colWidths[i]) {
      cellConfig.width = { size: colWidths[i], type: WidthType.PERCENTAGE };
    }
    return new TableCell(cellConfig);
  });

  const tableRows = [
    new TableRow({
      tableHeader: true,
      children: headerCells
    })
  ];

  rows.forEach((row, rIndex) => {
    const isAlt = rIndex % 2 === 1;
    const cells = row.map((c, i) => {
      const cellConfig = {
        margins: CELL_MARGINS,
        shading: { fill: isAlt ? COLORS.TABLE_ALT_BG : COLORS.WHITE, type: ShadingType.CLEAR },
        borders: {
          top: BORDER_STYLE,
          bottom: BORDER_STYLE,
          left: BORDER_STYLE,
          right: BORDER_STYLE
        },
        children: [
          new Paragraph({
            spacing: { before: 30, after: 30 },
            children: [
              new TextRun({
                text: String(c != null ? c : ''),
                size: 18,
                color: COLORS.DARK_GRAY,
                font: 'Arial'
              })
            ]
          })
        ]
      };
      if (colWidths[i]) {
        cellConfig.width = { size: colWidths[i], type: WidthType.PERCENTAGE };
      }
      return new TableCell(cellConfig);
    });

    tableRows.push(new TableRow({ children: cells }));
  });

  return new Table({
    width: { size: 100, type: WidthType.PERCENTAGE },
    rows: tableRows
  });
}

function pageBreak() {
  return new Paragraph({
    children: [new TextRun({ text: '', break: 1 })],
    pageBreakBefore: true
  });
}

module.exports = {
  COLORS,
  pTitle,
  pSubtitle,
  h1,
  h2,
  h3,
  p,
  bullet,
  callout,
  codeBlock,
  createTable,
  pageBreak
};

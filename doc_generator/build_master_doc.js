/**
 * AquaFlow Master Documentation Builder
 * Assembles all 12 chapters into a single comprehensive Word document (.docx).
 */
const fs = require('fs');
const path = require('path');
const { Document, Packer } = require('docx');

const { getChapter1And2 } = require('./chapter1_2');
const { getChapter3And4 } = require('./chapter3_4');
const { getChapter5And6 } = require('./chapter5_6');
const { getChapter7And8 } = require('./chapter7_8');
const { getChapter9And10 } = require('./chapter9_10');
const { getChapter11And12 } = require('./chapter11_12');

async function buildMasterDoc() {
  console.log('Compiling AquaFlow Enterprise Master System Documentation...');

  const allElements = [
    ...getChapter1And2(),
    ...getChapter3And4(),
    ...getChapter5And6(),
    ...getChapter7And8(),
    ...getChapter9And10(),
    ...getChapter11And12()
  ];

  console.log(`Total Document Blocks Compiled: ${allElements.length}`);

  const doc = new Document({
    creator: 'AquaFlow Platform Architecture & Engineering Team',
    title: 'AquaFlow Smart Water Management System - Complete System Documentation',
    description: 'Exhaustive architecture specification, database dictionary, user workflows, computational engines, and operations manual.',
    sections: [
      {
        properties: {
          page: {
            margin: {
              top: 1440,    // 1 inch
              bottom: 1440, // 1 inch
              left: 1440,   // 1 inch
              right: 1440   // 1 inch
            }
          }
        },
        children: allElements
      }
    ]
  });

  const buffer = await Packer.toBuffer(doc);
  const targetPath = path.resolve('p:\\REP', 'AquaFlow_Complete_System_Documentation.docx');
  fs.writeFileSync(targetPath, buffer);

  const stats = fs.statSync(targetPath);
  console.log(`\n============================================================`);
  console.log(`MASTER DOCUMENTATION GENERATED SUCCESSFULLY!`);
  console.log(`File Location: ${targetPath}`);
  console.log(`File Size: ${(stats.size / 1024).toFixed(2)} KB (${stats.size} bytes)`);
  console.log(`============================================================\n`);
}

buildMasterDoc().catch(err => {
  console.error('Fatal error building master documentation:', err);
  process.exit(1);
});

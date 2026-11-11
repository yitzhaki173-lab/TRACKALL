/**
 * PackTrack IL — Google Apps Script לסנכרון אוטומטי 24/7 של הזמנות חדשות מ-Gmail
 * הוראות התקנה (2 דקות, חינם לגמרי):
 * 1. היכנס לכתובת https://script.google.com בחשבון ה-Google שלך (yitzhaki173@gmail.com) ולחץ על "פרויקט חדש" (New project).
 * 2. מחק את הקוד הקיים, הדבק את הקוד הזה ושמור (Ctrl+S).
 * 3. לחץ למעלה על "פריסה" (Deploy) -> "פריסה חדשה" (New deployment).
 * 4. בחר בסוג "אפליקציית אינטרנט" (Web app), הגדר "למי יש גישה" ל-"כולם" (Anyone), ולחץ על Deploy.
 * 5. העתק את כתובת ה-Web App שקיבלת והדבק אותה בלשונית "סנכרון אוטומטי" באפליקציית PackTrack IL!
 */
function doGet() {
  var q = 'label:AliExpress OR label:Temu OR from:(postil.co.il OR chita-il.com OR cheetahint.com OR gaashwd.com OR ydm.co.il OR ksp.co.il OR ace.co.il) newer_than:30d';
  var threads = GmailApp.search(q, 0, 30);
  var packages = [];

  threads.forEach(function(t) {
    var msgs = t.getMessages();
    var msg = msgs[msgs.length - 1];
    var subj = msg.getSubject() || '';
    var body = msg.getPlainBody() || '';
    var combined = subj + ' ' + body;

    var trackMatch = combined.match(/\b(RS\d{9,10}[A-Z0-9]{1,2}|RN\d{9,10}Y|CN\d{12,16}|TMU\d{8,12}|JYDIL\d+YQ|AP\d{12,16}|\d{16})\b/i);
    if (trackMatch) {
      var codeMatch = body.match(/(?:[א-ת]\s*\d{3,5}|קוד איסוף\s*[:#-]?\s*[A-Z0-9-]+|חשבונית\s*[:#-]?\s*\d+)/i);
      var urlMatch = body.match(/(https?:\/\/[^\s)\]"']+)/i);
      var isDelivered = /תודה שאספת|has been delivered|נמסרה בהצלחה/i.test(combined);
      var isReady = /ready for pickup|ממתין ב|מוכנה לאיסוף/i.test(combined);

      packages.push({
        id: 'gas-' + msg.getId(),
        title: subj,
        store: /temu|gaash|ydm/i.test(combined) ? 'Temu' : (/ksp/i.test(combined) ? 'KSP' : (/ace/i.test(combined) ? 'ACE' : 'AliExpress')),
        trackingNumber: trackMatch[1].toUpperCase(),
        courier: /postil|דואר/i.test(combined) ? 'דואר ישראל' : (/chita|cheetah|צ'יטה/i.test(combined) ? 'צ׳יטה שליחויות' : 'משלוח בינלאומי'),
        status: isDelivered ? 'collected' : (isReady ? 'ready_for_pickup' : 'in_transit'),
        pickupCode: codeMatch ? codeMatch[0].trim() : 'עודכן אוטומטית מ-Gmail',
        pickupLocation: 'נסרק אוטומטית מחשבון ה-Gmail שלך',
        actionUrl: urlMatch ? urlMatch[1] : '',
        actionLabel: 'פתח קישור מהמייל',
        emailUrl: 'https://mail.google.com/mail/u/0/#all/' + t.getId(),
        updatedAt: Utilities.formatDate(msg.getDate(), 'Asia/Jerusalem', 'dd/MM/yyyy')
      });
    }
  });

  return ContentService.createTextOutput(JSON.stringify({ packages: packages }))
    .setMimeType(ContentService.MimeType.JSON);
}

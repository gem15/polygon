
Insert into KB_ZAK (ID,ID_SVH,ID_WMS,ID_USR,N_ZAK,ID_KLIENT)
 values ('0102315556','KB_SVH95476','10406','KB_USR99992','ТОРГОВЫЙ ДОМ РУСЬ','300254');
Insert into KB_ZAK (ID,ID_SVH,ID_WMS,ID_USR,N_ZAK,ID_KLIENT) values ('0102315640',null,'500000589',null,'Магнит РЦ Тула','300254');
Insert into KB_ZAK (ID,ID_SVH,ID_WMS,ID_USR,N_ZAK,ID_KLIENT) values ('0102315643',null,'7000036075',null,'Перекресток-онлайн Вешки','300254');
Insert into KB_ZAK (ID,ID_SVH,ID_WMS,ID_USR,N_ZAK,ID_KLIENT) values ('0102315727',null,'35666',null,'Зельгрос Одинцово','300254');

Insert into LOADS (HOLDER_ID,LINENO,ARTICLE,UPC,NAME,QTY) values ('10406',1,'MC780MN03A','2000001682395','Весы- анализатор тела MC-780MAN KG EU S WH','3');
Insert into LOADS (HOLDER_ID,LINENO,ARTICLE,UPC,NAME,QTY) values ('10406',2,'MC780MN01A','2000001682371','Весы- анализатор тела MC-780MAN KG EU P WH','2');
Insert into LOADS (HOLDER_ID,LINENO,ARTICLE,UPC,NAME,QTY) values ('10406',3,'MC58CA01D','2000001682364','Крепление весов к дисплею 580-CK75 COLUMN KIT MC580 75 CM','2');
Insert into LOADS (HOLDER_ID,LINENO,ARTICLE,UPC,NAME,QTY) values ('10406',4,'OP102RU001','2000001682401','Беспроводной сетевой адаптер','3');
Insert into LOADS (HOLDER_ID,LINENO,ARTICLE,UPC,NAME,QTY) values ('10406',5,'MC780MN02A','2000001682388','Весы- анализатор тела MC-780MAN KG EU P DG','3');
Insert into LOADS (HOLDER_ID,LINENO,ARTICLE,UPC,NAME,QTY) values ('10406',6,'ZZ3500TANITAPRO','2000001682418','Энергонезависимые твердотельные устройства хранения данных мини- или микроформата (известные как "флэш-карты памяти" или "электронные флэш-карты для хранения данных")   с записанной GMON - программой','4');
Insert into LOADS (HOLDER_ID,LINENO,ARTICLE,UPC,NAME,QTY) values ('10406',7,'MC580001D','2000001682357','Весы- анализатор тела MC-580 KG EU MAIN UNIT DG','4');


Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) --Pycb daily order
 values ('01023999636','1685295',to_date('15.07.21','DD.MM.RR'),'0102315556','0102314344','STOCK','Суточный заказ Хеллманн по пакетам PS');

Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023999446','1685200',to_date('28.06.21','DD.MM.RR'),'0102314343','0102314343','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023987499','1680370',to_date('23.06.21','DD.MM.RR'),'0102309059','0102309059','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023943345','1662433',to_date('08.06.21','DD.MM.RR'),'0102312705','0102312705','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023941662','1661836',to_date('07.06.21','DD.MM.RR'),'0102314343','0102314343','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023941522','1661765',to_date('07.06.21','DD.MM.RR'),'0102309059','0102309059','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023941509','1661758',to_date('07.06.21','DD.MM.RR'),'0102311005','0102311005','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023941117','1661570',to_date('07.06.21','DD.MM.RR'),'0102312705','0102312705','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023892217','1642312',to_date('19.05.21','DD.MM.RR'),'0102312705','0102312705','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023876354','1635968',to_date('13.05.21','DD.MM.RR'),'0102312705','0102312705','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023840636','1621898',to_date('29.04.21','DD.MM.RR'),'0102312705','0102312705','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023819087','1612945',to_date('19.04.21','DD.MM.RR'),'0102309059','0102309059','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023819066','1612943',to_date('19.04.21','DD.MM.RR'),'0102311005','0102311005','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023817728','1612436',to_date('19.04.21','DD.MM.RR'),'0102312705','0102312705','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023809221','1609182',to_date('15.04.21','DD.MM.RR'),'0102312705','0102312705','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023805585','1607607',to_date('14.04.21','DD.MM.RR'),'0102312705','0102312705','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023801902','1606153',to_date('12.04.21','DD.MM.RR'),'0102312705','0102312705','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023801843','1606123',to_date('12.04.21','DD.MM.RR'),'0102311005','0102311005','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023574970','1515880',to_date('18.12.20','DD.MM.RR'),'0102312705','0102312705','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023547703','1505196',to_date('04.12.20','DD.MM.RR'),'0102309059','0102309059','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023544256','1503769',to_date('03.12.20','DD.MM.RR'),'0102309059','0102309059','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023541541','1502663',to_date('02.12.20','DD.MM.RR'),'0102309059','0102309059','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023540107','1502122',to_date('01.12.20','DD.MM.RR'),'0102309059','0102309059','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');
Insert into KB_SPROS (ID,N_ZAKAZA,DT_ZAKAZ,ID_ZAK,ID_POK,N_GRUZ,USL) values ('01023534268','1499870',to_date('27.11.20','DD.MM.RR'),'0102309059','0102309059','HELLMAN_STOCK','Суточный заказ Хеллманн по пакетам PS');


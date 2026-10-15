-- =====================================================================
-- DATOS SEMILLA (solo perfil dev: application-dev.yml añade classpath:db/seed
-- a spring.flyway.locations). Nunca se cargan en prod.
--
-- Reglas que respeta este script:
-- - Sin ids fijos: toda relación se resuelve con subconsultas por email o nombre.
-- - Fechas relativas a CURRENT_DATE, así los estados siguen siendo coherentes
--   el día en que se ejecute (una PENDIENTE siempre empieza en el futuro, una
--   ENTREGADA está en curso, una DEVUELTA ya terminó...).
-- - CHECK de reservas (fechas de entrega/devolución según el estado) y la
--   restricción EXCLUDE: cada reserva que bloquea (PENDIENTE, ACEPTADA,
--   ENTREGADA) usa una herramienta distinta.
-- - Toda reserva ENTREGADA o DEVUELTA tiene un pago APROBADO (regla de V6+).
-- - Cada pago APROBADO tiene su factura, cuyo total (días × tarifa) es igual
--   al monto del pago, con la misma línea que emite la aplicación.
--
-- CONTRASEÑA de TODOS los usuarios semilla: Clave123*
-- Hash BCrypt (coste 10) generado con el BCryptPasswordEncoder de Spring
-- Security 7.0.7 (el mismo que usa la app) y verificado con matches():
--   $2a$10$.PnB.JXNH/wfQxF531bN8OogyTuFTWfRgMm2v1jsnhW18U6Yz4QMW
--
-- Usuarios para probar: admin@rentaherramientas.co (ADMIN),
-- carlos.rueda@ferreteriaeltornillo.co (PROVEEDOR), andres.rueda@gmail.com (CLIENTE).
-- =====================================================================

-- ------------------------------------------------------------- 1 ADMIN
INSERT INTO usuarios (nombre, email, password_hash, rol) VALUES
    ('Administración Renta de Herramientas', 'admin@rentaherramientas.co',
     '$2a$10$.PnB.JXNH/wfQxF531bN8OogyTuFTWfRgMm2v1jsnhW18U6Yz4QMW', 'ADMIN');

-- -------------------------------------------------------- 20 PROVEEDORES
WITH datos (nombre, email, razon_social, nit) AS (VALUES
    ('Carlos Rueda Pinzón',        'carlos.rueda@ferreteriaeltornillo.co',    'Ferretería El Tornillo SAS',              '900101001-3'),
    ('Martha Lucía Ardila',        'ventas@herramientasdeloriente.co',        'Herramientas del Oriente SAS',            '900101002-1'),
    ('Jorge Enrique Gómez',        'contacto@alquiequipossantander.co',       'Alquiequipos Santander SAS',              '900101003-8'),
    ('Diana Patricia Serrano',     'info@construequiposfloridablanca.co',     'Construequipos Floridablanca SAS',        '900101004-6'),
    ('Luis Alberto Mantilla',      'maquinariagiron@gmail.com',               'Maquinaria Girón Ltda',                   '804012345-2'),
    ('Sandra Milena Quintero',     'rentamaq.piedecuesta@gmail.com',          'Rentamaq Piedecuesta SAS',                '901234567-0'),
    ('Fabio Hernández Rojas',      'ventas@ferrecabecera.co',                 'Ferre Cabecera SAS',                      '900101007-4'),
    ('Claudia Marcela Duarte',     'andamios@equiposdelsur.co',               'Equipos y Andamios del Sur SAS',          '900101008-2'),
    ('Héctor Julio Prada',         'distribuidora@lallave.co',                'Distribuidora La Llave SAS',              '900101009-0'),
    ('Paola Andrea Villamizar',    'alquiler@herramientasruitoque.co',        'Herramientas Ruitoque SAS',               '900101010-5'),
    ('Germán Darío Sanabria',      'contacto@electroherramientascanaveral.co','Electroherramientas Cañaveral SAS',       '900101011-3'),
    ('Ángela María Corzo',         'obra@alquileryobralagos.co',              'Alquiler y Obra Lagos SAS',               '900101012-1'),
    ('Ricardo Antonio Vargas',     'ventas@ferreteriasanfrancisco.co',        'Ferretería San Francisco SAS',            '900101013-8'),
    ('Yolanda Esperanza Díaz',     'info@maquiobrasprovenza.co',              'Maquiobras Provenza SAS',                 '900101014-6'),
    ('Óscar Iván Castellanos',     'herramientas.mutis@gmail.com',            'Herramientas Mutis SAS',                  '900101015-4'),
    ('Liliana Patricia Niño',      'equiposlarosita@gmail.com',               'Equipos La Rosita SAS',                   '900101016-2'),
    ('Mauricio Andrés Silva',      'ventas@ferreteriaelprado.co',             'Ferretería El Prado SAS',                 '900101017-0'),
    ('Gloria Inés Bautista',       'soluciones@obragiron.co',                 'Soluciones de Obra Girón SAS',            '900101018-7'),
    ('Edwin Fernando Calderón',    'taller@herramientarealdeminas.co',        'Taller y Herramienta Real de Minas SAS',  '900101019-5'),
    ('Natalia Andrea Pabón',       'comercial@megaequipossantander.co',       'Megaequipos Santander SAS',               '900101020-9')
),
nuevos AS (
    INSERT INTO usuarios (nombre, email, password_hash, rol)
    SELECT nombre, email, '$2a$10$.PnB.JXNH/wfQxF531bN8OogyTuFTWfRgMm2v1jsnhW18U6Yz4QMW', 'PROVEEDOR'
    FROM datos
    RETURNING id, email
)
INSERT INTO proveedores (usuario_id, razon_social, nit)
SELECT n.id, d.razon_social, d.nit
FROM nuevos n
JOIN datos d ON d.email = n.email;

-- ----------------------------------------------------------- 25 CLIENTES
WITH datos (nombre, email, documento, telefono, direccion) AS (VALUES
    ('Andrés Felipe Rueda Pinzón',   'andres.rueda@gmail.com',        '1098654321', '3157894561', 'Cra 33 # 48-25, Cabecera del Llano, Bucaramanga'),
    ('María Camila Ortiz Suárez',    'mcamila.ortiz@hotmail.com',     '1098712345', '3004567812', 'Calle 56 # 31-14, Sotomayor, Bucaramanga'),
    ('Juan Sebastián Pérez León',    'juanse.perez@gmail.com',        '1095812233', '3168892210', 'Cra 8 # 4-35, Cañaveral, Floridablanca'),
    ('Laura Valentina Moreno',       'laura.moreno@outlook.com',      '1098798456', '3014456789', 'Calle 200 # 12-40, Lagos del Cacique, Floridablanca'),
    ('Diego Alejandro Ramírez',      'diego.ramirez@gmail.com',       '1102356789', '3123345566', 'Cra 25 # 30-18, El Poblado, Girón'),
    ('Paula Andrea Gélvez',          'paula.gelvez@yahoo.com',        '1098623456', '3187765432', 'Calle 9 # 5-60, Centro, Piedecuesta'),
    ('Santiago Uribe Mantilla',      'santiago.uribe@gmail.com',      '1005328741', '3209981234', 'Cra 27 # 36-50, Mejoras Públicas, Bucaramanga'),
    ('Valentina Rojas Díaz',         'vale.rojas@gmail.com',          '1098756213', '3156672345', 'Calle 105 # 22-31, Provenza, Bucaramanga'),
    ('Camilo Andrés Jaimes',         'camilo.jaimes@hotmail.com',     '1095934567', '3002231987', 'Cra 6 # 10-22, Villabel, Floridablanca'),
    ('Natalia Sofía Cárdenas',       'natalia.cardenas@gmail.com',    '1098678901', '3176654321', 'Calle 48 # 28-09, Antonia Santos, Bucaramanga'),
    ('Felipe Augusto Torres',        'felipe.torres@outlook.com',     '91284567',   '3105543219', 'Cra 15 # 45-80, García Rovira, Bucaramanga'),
    ('Daniela Fernanda Acevedo',     'daniela.acevedo@gmail.com',     '1098734567', '3134432198', 'Calle 34 # 19-41, San Francisco, Bucaramanga'),
    ('Julián David Becerra',         'julian.becerra@gmail.com',      '1101234567', '3142219876', 'Cra 20 # 25-13, Portal de Girón, Girón'),
    ('Mariana Isabel Quintero',      'mariana.quintero@hotmail.com',  '1098790123', '3188891234', 'Calle 4 # 9-27, Barrio Hoyo Chiquito, Piedecuesta'),
    ('Sergio Esteban Ardila',        'sergio.ardila@gmail.com',       '1098701234', '3193345678', 'Cra 29 # 52-33, Cabecera, Bucaramanga'),
    ('Isabella Rincón Prada',        'isabella.rincon@gmail.com',     '1098645678', '3008876543', 'Calle 31 # 26-10, Bolarquí, Bucaramanga'),
    ('Mateo Nicolás Ballesteros',    'mateo.ballesteros@outlook.com', '1095876543', '3015567890', 'Cra 12 # 147-55, Cañaveral Oriental, Floridablanca'),
    ('Gabriela Alejandra Pinto',     'gabriela.pinto@gmail.com',      '1098667788', '3126678901', 'Calle 64 # 9-30, La Ceiba, Bucaramanga'),
    ('Nicolás Eduardo Delgado',      'nicolas.delgado@yahoo.com',     '1098712398', '3167789012', 'Cra 22 # 60-45, Conucos, Bucaramanga'),
    ('Sara Lucía Hernández',         'sara.hernandez@gmail.com',      '1102345671', '3178890123', 'Calle 42 # 25-16, Rincón de Girón, Girón'),
    ('Alejandro José Villamizar',    'alejandro.villamizar@gmail.com','1098689012', '3189901234', 'Cra 9 # 2-48, Paseo del Puente, Piedecuesta'),
    ('Catalina Andrea Serrano',      'catalina.serrano@hotmail.com',  '63512345',   '3190012345', 'Calle 52 # 35-70, Terrazas, Bucaramanga'),
    ('Tomás Felipe Navarro',         'tomas.navarro@gmail.com',       '1098745612', '3201123456', 'Cra 17 # 8-12, Altos de Bellavista, Floridablanca'),
    ('Juliana Marcela Castro',       'juliana.castro@outlook.com',    '1098656789', '3212234567', 'Calle 36 # 40-21, Álvarez, Bucaramanga'),
    ('David Ricardo Suárez',         'david.suarez@gmail.com',        '91512367',   '3223345678', 'Cra 3 # 15-90, Centro, Girón')
),
nuevos AS (
    INSERT INTO usuarios (nombre, email, password_hash, rol)
    SELECT nombre, email, '$2a$10$.PnB.JXNH/wfQxF531bN8OogyTuFTWfRgMm2v1jsnhW18U6Yz4QMW', 'CLIENTE'
    FROM datos
    RETURNING id, email
)
INSERT INTO clientes (usuario_id, documento, telefono, direccion)
SELECT n.id, d.documento, d.telefono, d.direccion
FROM nuevos n
JOIN datos d ON d.email = n.email;

-- ------------------------------------------------------- 20 CATEGORÍAS
INSERT INTO categorias (nombre, descripcion) VALUES
    ('Taladros y rotomartillos', 'Taladros percutores, atornilladores y rotomartillos SDS para perforar concreto, madera y metal.'),
    ('Corte', 'Sierras circulares, cortadoras y motosierras para madera, metal y concreto.'),
    ('Pulido y lijado', 'Pulidoras angulares y lijadoras orbitales o de banda para acabados.'),
    ('Construcción pesada', 'Equipos robustos para obra: demoledores, mezcladoras y carretillas de carga.'),
    ('Andamios y escaleras', 'Andamios certificados y escaleras para trabajo en alturas.'),
    ('Jardinería', 'Cortacéspedes, guadañadoras, motosierras y herramienta de jardín.'),
    ('Soldadura', 'Soldadoras inversoras y MIG para trabajos de metalmecánica.'),
    ('Neumática', 'Compresores de aire y herramientas que funcionan con aire comprimido.'),
    ('Generación eléctrica', 'Plantas eléctricas y generadores para obras sin conexión a la red.'),
    ('Limpieza a presión', 'Hidrolavadoras eléctricas y a gasolina para fachadas, pisos y vehículos.'),
    ('Medición y nivelación', 'Niveles láser y medidores de distancia para obra y remodelación.'),
    ('Herramienta manual', 'Martillos, llaves, destornilladores y juegos de herramienta de mano.'),
    ('Carpintería', 'Equipos para cortar, perforar y dar acabado a la madera.'),
    ('Plomería', 'Llaves de tubo y herramienta para instalaciones hidráulicas.'),
    ('Pintura', 'Pistolas de pintura y compresores para acabados.'),
    ('Electricidad', 'Herramienta para instalaciones eléctricas residenciales.'),
    ('Demolición', 'Martillos demoledores y rotomartillos para romper concreto y mampostería.'),
    ('Transporte de materiales', 'Carretillas y equipos para mover materiales en obra.'),
    ('Seguridad industrial', 'Equipos certificados para trabajo seguro en obra y en alturas.'),
    ('Concreto y mampostería', 'Mezcladoras y cortadoras para trabajos con concreto y ladrillo.');

-- ------------------- 40 HERRAMIENTAS + 40 IMÁGENES + 66 FILAS EN herramienta_categorias
-- Estados: 33 DISPONIBLE, 2 EN_MANTENIMIENTO, 3 DANADA, 2 INACTIVA.
-- Las imágenes son las ilustraciones de /static/img/seed (rutas relativas a la API).
WITH datos (nombre, proveedor_email, descripcion, tarifa, estado, imagen, categorias) AS (VALUES
    ('Taladro percutor Bosch GSB 13 RE 650W', 'carlos.rueda@ferreteriaeltornillo.co',
     'Taladro percutor con mandril de 13 mm, velocidad variable y reversa. Incluye juego de brocas.', 25000, 'DISPONIBLE', 'taladro',
     ARRAY['Taladros y rotomartillos', 'Carpintería']),
    ('Atornillador inalámbrico DeWalt DCD771 20V', 'carlos.rueda@ferreteriaeltornillo.co',
     'Atornillador/taladro inalámbrico con dos baterías de 20V y cargador.', 30000, 'DISPONIBLE', 'taladro',
     ARRAY['Taladros y rotomartillos', 'Electricidad']),
    ('Rotomartillo SDS Plus Makita HR2470', 'ventas@herramientasdeloriente.co',
     'Rotomartillo de 780W con tres modos: taladro, percusión y cincelado. Ideal para concreto.', 45000, 'DISPONIBLE', 'rotomartillo',
     ARRAY['Taladros y rotomartillos', 'Demolición']),
    ('Martillo demoledor Bosch GSH 11 E', 'ventas@herramientasdeloriente.co',
     'Demoledor de 1500W y 16,8 J de impacto para romper placas y muros.', 90000, 'DISPONIBLE', 'rotomartillo',
     ARRAY['Demolición', 'Construcción pesada']),
    ('Pulidora angular DeWalt 4 1/2 pulgadas 900W', 'contacto@alquiequipossantander.co',
     'Pulidora compacta para corte y desbaste de metal y cerámica. Incluye guarda y disco.', 22000, 'DISPONIBLE', 'pulidora',
     ARRAY['Pulido y lijado', 'Corte']),
    ('Pulidora angular Makita 7 pulgadas 2200W', 'contacto@alquiequipossantander.co',
     'Pulidora de alta potencia para cortes profundos en concreto y metal.', 32000, 'EN_MANTENIMIENTO', 'pulidora',
     ARRAY['Pulido y lijado', 'Corte']),
    ('Sierra circular Makita 5007MG 7 1/4 pulgadas', 'info@construequiposfloridablanca.co',
     'Sierra circular de 1800W con base de magnesio y guía paralela.', 35000, 'DISPONIBLE', 'sierra-circular',
     ARRAY['Corte', 'Carpintería']),
    ('Sierra circular inalámbrica Milwaukee M18', 'info@construequiposfloridablanca.co',
     'Sierra inalámbrica de 18V con freno eléctrico. Incluye batería de 5 Ah.', 40000, 'DISPONIBLE', 'sierra-circular',
     ARRAY['Corte', 'Carpintería']),
    ('Lijadora orbital Bosch GEX 125-1 AE', 'maquinariagiron@gmail.com',
     'Lijadora roto-orbital de 250W con extracción de polvo y lijas de varios granos.', 20000, 'DISPONIBLE', 'lijadora',
     ARRAY['Pulido y lijado', 'Carpintería']),
    ('Lijadora de banda Makita 9403', 'maquinariagiron@gmail.com',
     'Lijadora de banda de 4 pulgadas y 1200W para desbaste rápido de madera.', 28000, 'DANADA', 'lijadora',
     ARRAY['Pulido y lijado']),
    ('Kit de martillos y mazos Stanley 5 piezas', 'rentamaq.piedecuesta@gmail.com',
     'Martillo de uña, martillo de bola, mazo de goma, porra de 4 lb y cincel.', 12000, 'DISPONIBLE', 'martillo',
     ARRAY['Herramienta manual', 'Demolición']),
    ('Juego de llaves combinadas Truper 25 piezas', 'rentamaq.piedecuesta@gmail.com',
     'Llaves mixtas en milímetros y pulgadas con estuche.', 10000, 'DISPONIBLE', 'llave-inglesa',
     ARRAY['Herramienta manual', 'Plomería']),
    ('Llave de tubo Ridgid 24 pulgadas', 'ventas@ferrecabecera.co',
     'Llave de tubo de hierro dúctil para tubería de hasta 3 pulgadas.', 15000, 'DISPONIBLE', 'llave-inglesa',
     ARRAY['Plomería', 'Herramienta manual']),
    ('Kit de destornilladores de precisión Wera', 'ventas@ferrecabecera.co',
     'Juego de 12 destornilladores aislados de pala, estrella y torx.', 9000, 'DISPONIBLE', 'destornillador',
     ARRAY['Herramienta manual', 'Electricidad']),
    ('Escalera de tijera en aluminio 8 pasos', 'andamios@equiposdelsur.co',
     'Escalera de tijera de 2,4 m con capacidad de 136 kg y bandeja portaherramientas.', 18000, 'DISPONIBLE', 'escalera',
     ARRAY['Andamios y escaleras']),
    ('Escalera extensible en fibra de vidrio 28 pies', 'andamios@equiposdelsur.co',
     'Escalera dieléctrica extensible de 8,5 m, apta para trabajos eléctricos.', 30000, 'DISPONIBLE', 'escalera',
     ARRAY['Andamios y escaleras', 'Electricidad']),
    ('Carretilla buggy 90 litros llanta neumática', 'distribuidora@lallave.co',
     'Carretilla de obra con platón metálico de 90 litros y llanta neumática.', 15000, 'DISPONIBLE', 'carretilla',
     ARRAY['Transporte de materiales', 'Construcción pesada']),
    ('Carretilla reforzada Truper 6 pies cúbicos', 'distribuidora@lallave.co',
     'Carretilla de chasis reforzado. Retirada del catálogo por renovación de flota.', 13000, 'INACTIVA', 'carretilla',
     ARRAY['Transporte de materiales']),
    ('Mezcladora de concreto 1 1/2 bultos a gasolina', 'alquiler@herramientasruitoque.co',
     'Mezcladora con motor de 9 HP y tambor para bulto y medio de cemento.', 85000, 'DISPONIBLE', 'mezcladora',
     ARRAY['Concreto y mampostería', 'Construcción pesada']),
    ('Mezcladora de concreto eléctrica 150 litros', 'alquiler@herramientasruitoque.co',
     'Mezcladora eléctrica de 150 litros, ideal para remodelaciones.', 65000, 'DISPONIBLE', 'mezcladora',
     ARRAY['Concreto y mampostería']),
    ('Compresor de aire 50 litros 2 HP', 'contacto@electroherramientascanaveral.co',
     'Compresor lubricado de 50 litros y 115 PSI con manguera de 10 m.', 40000, 'DISPONIBLE', 'compresor',
     ARRAY['Neumática', 'Pintura']),
    ('Compresor de aire portátil 24 litros', 'contacto@electroherramientascanaveral.co',
     'Compresor libre de aceite de 24 litros para inflado y pintura liviana.', 30000, 'DISPONIBLE', 'compresor',
     ARRAY['Neumática']),
    ('Generador eléctrico a gasolina 3500W', 'obra@alquileryobralagos.co',
     'Planta eléctrica de 3500W con dos tomas de 120V y autonomía de 10 horas.', 70000, 'DISPONIBLE', 'generador',
     ARRAY['Generación eléctrica']),
    ('Planta eléctrica inverter Honda EU22i 2200W', 'obra@alquileryobralagos.co',
     'Generador inverter silencioso, seguro para equipos electrónicos.', 95000, 'EN_MANTENIMIENTO', 'generador',
     ARRAY['Generación eléctrica']),
    ('Hidrolavadora Kärcher K5 2100 PSI', 'ventas@ferreteriasanfrancisco.co',
     'Hidrolavadora eléctrica de 2100 PSI con lanza turbo y aplicador de detergente.', 45000, 'DISPONIBLE', 'hidrolavadora',
     ARRAY['Limpieza a presión']),
    ('Hidrolavadora a gasolina 3000 PSI', 'ventas@ferreteriasanfrancisco.co',
     'Hidrolavadora de 3000 PSI con motor de 6,5 HP para fachadas y pisos.', 75000, 'DISPONIBLE', 'hidrolavadora',
     ARRAY['Limpieza a presión']),
    ('Andamio tubular certificado cuerpo 1,5 x 1,5 m', 'info@maquiobrasprovenza.co',
     'Cuerpo de andamio certificado con crucetas, plataforma y barandas.', 20000, 'DISPONIBLE', 'andamio',
     ARRAY['Andamios y escaleras', 'Construcción pesada', 'Seguridad industrial']),
    ('Andamio multidireccional con ruedas', 'info@maquiobrasprovenza.co',
     'Torre de andamio móvil con ruedas con freno, para cielos rasos y fachadas.', 35000, 'DISPONIBLE', 'andamio',
     ARRAY['Andamios y escaleras']),
    ('Juego de pala cuadrada y pica de construcción', 'herramientas.mutis@gmail.com',
     'Pala cuadrada, pala redonda y pica con mango de madera.', 8000, 'DISPONIBLE', 'pala',
     ARRAY['Jardinería', 'Herramienta manual']),
    ('Ahoyador manual para postes', 'herramientas.mutis@gmail.com',
     'Ahoyador de doble pala para huecos de cercas y postes.', 10000, 'DANADA', 'pala',
     ARRAY['Jardinería']),
    ('Soldadora inversora Lincoln 200A', 'equiposlarosita@gmail.com',
     'Soldadora inversora de electrodo revestido de 200A, bivoltaje.', 55000, 'DISPONIBLE', 'soldadora',
     ARRAY['Soldadura']),
    ('Soldadora MIG Miller 140A', 'equiposlarosita@gmail.com',
     'Soldadora MIG de 140A con regulador y careta fotosensible.', 70000, 'DISPONIBLE', 'soldadora',
     ARRAY['Soldadura', 'Seguridad industrial']),
    ('Cortacésped a gasolina Honda HRX217', 'ventas@ferreteriaelprado.co',
     'Cortacésped de 21 pulgadas con recolector y altura de corte ajustable.', 50000, 'DISPONIBLE', 'cortacesped',
     ARRAY['Jardinería']),
    ('Guadañadora Stihl FS 280', 'ventas@ferreteriaelprado.co',
     'Guadañadora profesional con disco y cabezal de nailon, arnés incluido.', 45000, 'DISPONIBLE', 'cortacesped',
     ARRAY['Jardinería']),
    ('Nivel láser autonivelante Bosch GLL 3-80', 'soluciones@obragiron.co',
     'Nivel láser de tres planos a 360 grados con trípode y receptor.', 40000, 'DISPONIBLE', 'nivel-laser',
     ARRAY['Medición y nivelación']),
    ('Medidor láser de distancia Leica Disto D2', 'soluciones@obragiron.co',
     'Medidor láser de hasta 100 m con cálculo de áreas y volúmenes.', 20000, 'DISPONIBLE', 'nivel-laser',
     ARRAY['Medición y nivelación']),
    ('Motosierra Stihl MS 250', 'taller@herramientarealdeminas.co',
     'Motosierra a gasolina con espada de 18 pulgadas y cadena afilada.', 60000, 'DISPONIBLE', 'motosierra',
     ARRAY['Jardinería', 'Corte']),
    ('Motosierra eléctrica Makita UC4041A', 'taller@herramientarealdeminas.co',
     'Motosierra eléctrica de 1800W con espada de 16 pulgadas.', 40000, 'DANADA', 'motosierra',
     ARRAY['Jardinería', 'Corte']),
    ('Pistola de pintura HVLP Graco', 'comercial@megaequipossantander.co',
     'Pistola de pintura de alto volumen y baja presión para acabados finos.', 30000, 'DISPONIBLE', 'compresor',
     ARRAY['Pintura', 'Neumática']),
    ('Cortadora de concreto a gasolina Husqvarna K770', 'comercial@megaequipossantander.co',
     'Cortadora de disco de 14 pulgadas. Fuera de servicio definitivo.', 110000, 'INACTIVA', 'sierra-circular',
     ARRAY['Corte', 'Concreto y mampostería', 'Demolición'])
),
nuevas AS (
    INSERT INTO herramientas (proveedor_id, nombre, descripcion, tarifa_diaria, moneda, estado)
    SELECT (SELECT u.id FROM usuarios u WHERE u.email = d.proveedor_email),
           d.nombre, d.descripcion, d.tarifa, 'COP', d.estado
    FROM datos d
    RETURNING id, nombre
),
imagenes AS (
    INSERT INTO herramienta_imagenes (herramienta_id, url, orden)
    SELECT n.id, '/img/seed/' || d.imagen || '.svg', 1
    FROM nuevas n
    JOIN datos d ON d.nombre = n.nombre
)
INSERT INTO herramienta_categorias (herramienta_id, categoria_id)
SELECT n.id, (SELECT c.id FROM categorias c WHERE c.nombre = cat.nombre)
FROM nuevas n
JOIN datos d ON d.nombre = n.nombre
CROSS JOIN LATERAL unnest(d.categorias) AS cat (nombre);

-- ---------------------------------------------------------- 30 RESERVAS
-- inicio = días desde hoy; precio = tarifa actual × días (foto del momento).
-- 12 DEVUELTA, 5 ENTREGADA, 5 ACEPTADA, 3 PENDIENTE, 2 RECHAZADA, 3 VENCIDA.
-- Cada par (herramienta, cliente) es único: así los pagos y daños la encuentran.
WITH datos (herramienta, cliente_email, inicio, dias, estado) AS (VALUES
    ('Taladro percutor Bosch GSB 13 RE 650W',          'andres.rueda@gmail.com',         -60, 3, 'DEVUELTA'),
    ('Rotomartillo SDS Plus Makita HR2470',            'mcamila.ortiz@hotmail.com',      -55, 4, 'DEVUELTA'),
    ('Pulidora angular DeWalt 4 1/2 pulgadas 900W',    'juanse.perez@gmail.com',         -50, 2, 'DEVUELTA'),
    ('Sierra circular Makita 5007MG 7 1/4 pulgadas',   'laura.moreno@outlook.com',       -45, 3, 'DEVUELTA'),
    ('Lijadora de banda Makita 9403',                  'diego.ramirez@gmail.com',        -40, 5, 'DEVUELTA'),
    ('Mezcladora de concreto 1 1/2 bultos a gasolina', 'paula.gelvez@yahoo.com',         -38, 2, 'DEVUELTA'),
    ('Generador eléctrico a gasolina 3500W',           'santiago.uribe@gmail.com',       -35, 3, 'DEVUELTA'),
    ('Hidrolavadora Kärcher K5 2100 PSI',              'vale.rojas@gmail.com',           -30, 2, 'DEVUELTA'),
    ('Ahoyador manual para postes',                    'camilo.jaimes@hotmail.com',      -28, 4, 'DEVUELTA'),
    ('Soldadora inversora Lincoln 200A',               'natalia.cardenas@gmail.com',     -25, 3, 'DEVUELTA'),
    ('Motosierra eléctrica Makita UC4041A',            'felipe.torres@outlook.com',      -20, 2, 'DEVUELTA'),
    ('Taladro percutor Bosch GSB 13 RE 650W',          'daniela.acevedo@gmail.com',      -15, 3, 'DEVUELTA'),
    ('Atornillador inalámbrico DeWalt DCD771 20V',     'julian.becerra@gmail.com',        -2, 5, 'ENTREGADA'),
    ('Martillo demoledor Bosch GSH 11 E',              'mariana.quintero@hotmail.com',    -1, 4, 'ENTREGADA'),
    ('Escalera de tijera en aluminio 8 pasos',         'sergio.ardila@gmail.com',         -3, 6, 'ENTREGADA'),
    ('Compresor de aire 50 litros 2 HP',               'isabella.rincon@gmail.com',       -2, 4, 'ENTREGADA'),
    ('Cortacésped a gasolina Honda HRX217',            'mateo.ballesteros@outlook.com',    0, 3, 'ENTREGADA'),
    ('Sierra circular Makita 5007MG 7 1/4 pulgadas',   'gabriela.pinto@gmail.com',         3, 3, 'ACEPTADA'),
    ('Lijadora orbital Bosch GEX 125-1 AE',            'nicolas.delgado@yahoo.com',        5, 2, 'ACEPTADA'),
    ('Carretilla buggy 90 litros llanta neumática',    'sara.hernandez@gmail.com',         4, 3, 'ACEPTADA'),
    ('Hidrolavadora a gasolina 3000 PSI',              'alejandro.villamizar@gmail.com',   6, 2, 'ACEPTADA'),
    ('Nivel láser autonivelante Bosch GLL 3-80',       'catalina.serrano@hotmail.com',     7, 4, 'ACEPTADA'),
    ('Kit de martillos y mazos Stanley 5 piezas',      'tomas.navarro@gmail.com',          2, 2, 'PENDIENTE'),
    ('Andamio tubular certificado cuerpo 1,5 x 1,5 m', 'juliana.castro@outlook.com',       8, 5, 'PENDIENTE'),
    ('Motosierra Stihl MS 250',                        'david.suarez@gmail.com',          10, 3, 'PENDIENTE'),
    ('Sierra circular inalámbrica Milwaukee M18',      'andres.rueda@gmail.com',           4, 2, 'RECHAZADA'),
    ('Juego de llaves combinadas Truper 25 piezas',    'mcamila.ortiz@hotmail.com',       -5, 2, 'RECHAZADA'),
    ('Llave de tubo Ridgid 24 pulgadas',               'juanse.perez@gmail.com',         -12, 2, 'VENCIDA'),
    ('Escalera extensible en fibra de vidrio 28 pies', 'laura.moreno@outlook.com',        -9, 3, 'VENCIDA'),
    ('Compresor de aire portátil 24 litros',           'diego.ramirez@gmail.com',         -6, 2, 'VENCIDA')
),
calculo AS (
    SELECT h.id AS herramienta_id,
           (SELECT u.id FROM usuarios u WHERE u.email = d.cliente_email) AS cliente_id,
           CURRENT_DATE + d.inicio AS fecha_inicio,
           CURRENT_DATE + d.inicio + d.dias - 1 AS fecha_fin,
           d.estado,
           h.tarifa_diaria * d.dias AS precio_total
    FROM datos d
    JOIN herramientas h ON h.nombre = d.herramienta
)
INSERT INTO reservas (herramienta_id, cliente_id, fecha_inicio, fecha_fin, estado, precio_total, moneda,
                      fecha_entrega, fecha_devolucion, creado_en)
SELECT herramienta_id, cliente_id, fecha_inicio, fecha_fin, estado, precio_total, 'COP',
       CASE WHEN estado IN ('ENTREGADA', 'DEVUELTA') THEN fecha_inicio + TIME '09:00' END,
       CASE WHEN estado = 'DEVUELTA' THEN fecha_fin + TIME '17:30' END,
       LEAST(fecha_inicio - 3, CURRENT_DATE - 1) + TIME '10:15'
FROM calculo;

-- ------------------------------------------------------------- 24 PAGOS
-- 20 APROBADOS (todas las DEVUELTA y ENTREGADA + 3 ACEPTADA), 2 PENDIENTES en
-- EFECTIVO (ACEPTADA, esperan confirmación del proveedor) y 2 RECHAZADOS que el
-- cliente reintentó con éxito. Monto = precio total de la reserva. Un solo pago
-- vigente por reserva (uq_pagos_reserva_vigente).
WITH datos (herramienta, cliente_email, metodo, estado, n) AS (VALUES
    ('Taladro percutor Bosch GSB 13 RE 650W',          'andres.rueda@gmail.com',         'TARJETA',  'APROBADO',   1),
    ('Rotomartillo SDS Plus Makita HR2470',            'mcamila.ortiz@hotmail.com',      'TARJETA',  'RECHAZADO',  2),
    ('Rotomartillo SDS Plus Makita HR2470',            'mcamila.ortiz@hotmail.com',      'PSE',      'APROBADO',   3),
    ('Pulidora angular DeWalt 4 1/2 pulgadas 900W',    'juanse.perez@gmail.com',         'PSE',      'APROBADO',   4),
    ('Sierra circular Makita 5007MG 7 1/4 pulgadas',   'laura.moreno@outlook.com',       'EFECTIVO', 'APROBADO',   5),
    ('Lijadora de banda Makita 9403',                  'diego.ramirez@gmail.com',        'TARJETA',  'APROBADO',   6),
    ('Mezcladora de concreto 1 1/2 bultos a gasolina', 'paula.gelvez@yahoo.com',         'PSE',      'APROBADO',   7),
    ('Generador eléctrico a gasolina 3500W',           'santiago.uribe@gmail.com',       'TARJETA',  'APROBADO',   8),
    ('Hidrolavadora Kärcher K5 2100 PSI',              'vale.rojas@gmail.com',           'EFECTIVO', 'RECHAZADO',  9),
    ('Hidrolavadora Kärcher K5 2100 PSI',              'vale.rojas@gmail.com',           'TARJETA',  'APROBADO',  10),
    ('Ahoyador manual para postes',                    'camilo.jaimes@hotmail.com',      'EFECTIVO', 'APROBADO',  11),
    ('Soldadora inversora Lincoln 200A',               'natalia.cardenas@gmail.com',     'PSE',      'APROBADO',  12),
    ('Motosierra eléctrica Makita UC4041A',            'felipe.torres@outlook.com',      'TARJETA',  'APROBADO',  13),
    ('Taladro percutor Bosch GSB 13 RE 650W',          'daniela.acevedo@gmail.com',      'PSE',      'APROBADO',  14),
    ('Atornillador inalámbrico DeWalt DCD771 20V',     'julian.becerra@gmail.com',       'TARJETA',  'APROBADO',  15),
    ('Martillo demoledor Bosch GSH 11 E',              'mariana.quintero@hotmail.com',   'EFECTIVO', 'APROBADO',  16),
    ('Escalera de tijera en aluminio 8 pasos',         'sergio.ardila@gmail.com',        'PSE',      'APROBADO',  17),
    ('Compresor de aire 50 litros 2 HP',               'isabella.rincon@gmail.com',      'TARJETA',  'APROBADO',  18),
    ('Cortacésped a gasolina Honda HRX217',            'mateo.ballesteros@outlook.com',  'TARJETA',  'APROBADO',  19),
    ('Sierra circular Makita 5007MG 7 1/4 pulgadas',   'gabriela.pinto@gmail.com',       'PSE',      'APROBADO',  20),
    ('Lijadora orbital Bosch GEX 125-1 AE',            'nicolas.delgado@yahoo.com',      'TARJETA',  'APROBADO',  21),
    ('Carretilla buggy 90 litros llanta neumática',    'sara.hernandez@gmail.com',       'EFECTIVO', 'APROBADO',  22),
    ('Hidrolavadora a gasolina 3000 PSI',              'alejandro.villamizar@gmail.com', 'EFECTIVO', 'PENDIENTE', 23),
    ('Nivel láser autonivelante Bosch GLL 3-80',       'catalina.serrano@hotmail.com',   'EFECTIVO', 'PENDIENTE', 24)
),
calculo AS (
    SELECT r.id AS reserva_id, r.precio_total, d.metodo, d.estado, d.n,
           CASE WHEN d.estado = 'RECHAZADO'
                THEN (r.fecha_inicio - 2) + TIME '11:00'
                ELSE LEAST((r.fecha_inicio - 1) + TIME '15:00', date_trunc('minute', LOCALTIMESTAMP) - INTERVAL '2 hours')
           END AS fecha
    FROM datos d
    JOIN herramientas h ON h.nombre = d.herramienta
    JOIN reservas r ON r.herramienta_id = h.id
                   AND r.cliente_id = (SELECT u.id FROM usuarios u WHERE u.email = d.cliente_email)
)
INSERT INTO pagos (reserva_id, monto, moneda, metodo, estado, referencia, fecha)
SELECT reserva_id, precio_total, 'COP', metodo, estado,
       'PAG-' || to_char(fecha, 'YYYY') || '-SEED' || lpad(n::text, 4, '0'),
       fecha
FROM calculo;

-- ---------------------------------------------- 20 FACTURAS + 20 LÍNEAS
-- Una por pago APROBADO, numeradas con la misma SEQUENCE que usa la aplicación
-- (en orden de fecha de pago), así la numeración no se repite después.
INSERT INTO facturas (numero, pago_id, moneda, fecha_emision)
SELECT 'FAC-' || to_char(x.fecha, 'YYYY') || '-' || lpad(nextval('seq_facturas')::text, 6, '0'),
       x.id, x.moneda, x.fecha + INTERVAL '1 minute'
FROM (SELECT p.id, p.moneda, p.fecha
      FROM pagos p
      WHERE p.estado = 'APROBADO' AND p.referencia LIKE 'PAG-%-SEED%'
      ORDER BY p.fecha, p.id) AS x;

-- Misma línea que emite la aplicación: cantidad = días, valor unitario = tarifa
-- pactada (precio / días), así SUM(cantidad × valor_unitario) = monto del pago.
INSERT INTO factura_lineas (factura_id, descripcion, cantidad, valor_unitario)
SELECT f.id,
       'Alquiler de ' || h.nombre || ' del ' || to_char(r.fecha_inicio, 'DD/MM/YYYY')
           || ' al ' || to_char(r.fecha_fin, 'DD/MM/YYYY'),
       r.fecha_fin - r.fecha_inicio + 1,
       round(r.precio_total / (r.fecha_fin - r.fecha_inicio + 1), 2)
FROM facturas f
JOIN pagos p ON p.id = f.pago_id
JOIN reservas r ON r.id = p.reserva_id
JOIN herramientas h ON h.id = r.herramienta_id
WHERE p.referencia LIKE 'PAG-%-SEED%';

-- Ajuste explícito de la secuencia: el siguiente número que emita la app sigue
-- al mayor consecutivo existente (por si alguna factura se insertó sin nextval).
SELECT setval('seq_facturas', m, true)
FROM (SELECT max(substring(numero FROM '([0-9]+)$')::bigint) AS m FROM facturas) AS s
WHERE m IS NOT NULL;

-- ---------------------------------------------------- 20 REPORTES DE DAÑO
-- Solo sobre reservas DEVUELTA, unas horas después de la devolución. Las tres
-- herramientas con más daños quedaron DANADA en el catálogo.
WITH datos (herramienta, cliente_email, descripcion, costo, horas) AS (VALUES
    ('Lijadora de banda Makita 9403',                  'diego.ramirez@gmail.com',   'Banda de lija rota y rodillo tensor desalineado.',               45000.00::numeric, 1),
    ('Lijadora de banda Makita 9403',                  'diego.ramirez@gmail.com',   'Carcasa del motor fisurada por golpe.',                           80000.00, 2),
    ('Lijadora de banda Makita 9403',                  'diego.ramirez@gmail.com',   'Bolsa recolectora de polvo rasgada.',                             NULL, 3),
    ('Ahoyador manual para postes',                    'camilo.jaimes@hotmail.com', 'Mango derecho doblado.',                                          30000.00, 1),
    ('Ahoyador manual para postes',                    'camilo.jaimes@hotmail.com', 'Pala izquierda con el filo mellado.',                             15000.00, 2),
    ('Ahoyador manual para postes',                    'camilo.jaimes@hotmail.com', 'Remache de unión flojo.',                                         NULL, 4),
    ('Motosierra eléctrica Makita UC4041A',            'felipe.torres@outlook.com', 'Cadena partida y espada con desgaste irregular.',                 95000.00, 1),
    ('Motosierra eléctrica Makita UC4041A',            'felipe.torres@outlook.com', 'Cable de alimentación pelado cerca del enchufe.',                 25000.00, 2),
    ('Motosierra eléctrica Makita UC4041A',            'felipe.torres@outlook.com', 'Freno de cadena no responde.',                                    NULL, 3),
    ('Taladro percutor Bosch GSB 13 RE 650W',          'andres.rueda@gmail.com',    'Mandril con juego lateral.',                                      35000.00, 1),
    ('Taladro percutor Bosch GSB 13 RE 650W',          'andres.rueda@gmail.com',    'Faltan dos brocas del juego entregado.',                          18000.00, 2),
    ('Pulidora angular DeWalt 4 1/2 pulgadas 900W',    'juanse.perez@gmail.com',    'Guarda de protección doblada.',                                   22000.00, 1),
    ('Pulidora angular DeWalt 4 1/2 pulgadas 900W',    'juanse.perez@gmail.com',    'Disco de corte devuelto quebrado.',                               8000.00, 2),
    ('Sierra circular Makita 5007MG 7 1/4 pulgadas',   'laura.moreno@outlook.com',  'Guía paralela extraviada.',                                       40000.00, 1),
    ('Sierra circular Makita 5007MG 7 1/4 pulgadas',   'laura.moreno@outlook.com',  'Base con rayones profundos.',                                     NULL, 2),
    ('Mezcladora de concreto 1 1/2 bultos a gasolina', 'paula.gelvez@yahoo.com',    'Tambor devuelto con concreto fraguado en el interior.',           60000.00, 2),
    ('Generador eléctrico a gasolina 3500W',           'santiago.uribe@gmail.com',  'Toma de 120V quemada por sobrecarga.',                            50000.00, 1),
    ('Hidrolavadora Kärcher K5 2100 PSI',              'vale.rojas@gmail.com',      'Boquilla turbo obstruida.',                                       12000.00, 1),
    ('Soldadora inversora Lincoln 200A',               'natalia.cardenas@gmail.com','Pinza porta electrodo con aislamiento dañado.',                   20000.00, 1),
    ('Taladro percutor Bosch GSB 13 RE 650W',          'daniela.acevedo@gmail.com', 'Interruptor de velocidad variable intermitente.',                 NULL, 2)
)
INSERT INTO reportes_danio (reserva_id, descripcion, costo_estimado, reportado_en)
SELECT r.id, d.descripcion, d.costo, r.fecha_devolucion + make_interval(hours => d.horas)
FROM datos d
JOIN herramientas h ON h.nombre = d.herramienta
JOIN reservas r ON r.herramienta_id = h.id
               AND r.cliente_id = (SELECT u.id FROM usuarios u WHERE u.email = d.cliente_email)
               AND r.estado = 'DEVUELTA';

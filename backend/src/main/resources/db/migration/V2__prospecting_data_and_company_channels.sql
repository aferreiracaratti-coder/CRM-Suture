ALTER TABLE company
    ADD COLUMN address VARCHAR(500),
    ADD COLUMN phone VARCHAR(80),
    ADD COLUMN whatsapp VARCHAR(80),
    ADD COLUMN email VARCHAR(250);

ALTER TABLE lead
    ADD COLUMN priority VARCHAR(4),
    ADD COLUMN data_quality VARCHAR(20);

CREATE TABLE prospecting_exclusion (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tenant_id UUID NOT NULL REFERENCES tenant(id),
    company_name VARCHAR(200) NOT NULL,
    reason VARCHAR(200) NOT NULL DEFAULT 'DO_NOT_CONTACT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    UNIQUE (tenant_id, company_name)
);

-- Datos aportados por Suture: se preservan como base de prospección, no como datos de demostración.
INSERT INTO company (id, tenant_id, name, website, address, phone, whatsapp, email, industry, city, country, source)
VALUES
('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', 'Celisano', 'https://www.celisano.com.uy', NULL, '+598 92 244 124', NULL, 'info@celisano.com.uy', 'Alimentos / distribución', 'Salto', 'Uruguay', 'PROSPECCION_DIRECTA'),
('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', 'Hotel Los Cedros', 'https://www.loscedros.com.uy', 'Uruguay 657', '+598 4733 3984', '+598 98 031 290', 'loscedros@loscedros.com.uy', 'Hotelería / eventos', 'Salto', 'Uruguay', 'REFERIDO_PERSONAL'),
('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', 'Hotel Manantiales Termal', 'https://manantialestermal.com', NULL, '+598 4736 9832', '+598 98 675 457', 'hotelmanantiales@vera.com.uy', 'Hotelería', 'Termas del Daymán', 'Uruguay', 'PROSPECCION_DAYMAN'),
('10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', 'Bungalows Archi', 'https://archi.com.uy', NULL, '+598 4736 9512', NULL, NULL, 'Alojamiento turístico', 'Termas del Daymán', 'Uruguay', 'PROSPECCION_DAYMAN'),
('10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000001', 'Estudio Manzor', NULL, NULL, NULL, NULL, NULL, 'Estudio contable', NULL, 'Uruguay', 'REFERRAL'),
('10000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000001', 'Don Andrés', NULL, 'Av. Lucas Obes 1054 esq. 19 de Abril', '+598 2336 6418', NULL, 'clientes@donandres.uy', 'Gastronomía', 'Montevideo', 'Uruguay', 'PROSPECCION_VERIFICADA'),
('10000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000001', 'La Cocina de Pedro', NULL, 'Av. Gonzalo Ramírez 1483', '+598 93 507 618 / +598 2413 7453', '+598 99 813 923', 'reservas@lacocinadepedro.com.uy', 'Gastronomía', 'Montevideo', 'Uruguay', 'PROSPECCION_VERIFICADA'),
('10000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000001', 'Tandory Restaurante', NULL, 'Libertad 2851 esq. Ramón Masini', '+598 2709 6616', '+598 94 596 735', 'tandoryrestaurant@gmail.com', 'Gastronomía', 'Montevideo', 'Uruguay', 'PROSPECCION_VERIFICADA'),
('10000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000001', 'Primuseum', NULL, 'Pérez Castellano 1389 esq. Washington', '+598 92 125 698', NULL, 'info@primuseum.com', 'Turismo / gastronomía / experiencias', 'Montevideo', 'Uruguay', 'PROSPECCION_VERIFICADA'),
('10000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001', 'El Fogón', NULL, 'San José 1080', '+598 2900 0900', NULL, 'elfogon@elfogon.com.uy', 'Gastronomía', 'Montevideo', 'Uruguay', 'PROSPECCION_VERIFICADA'),
('10000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000001', 'Francis Restaurante', NULL, 'Luis de la Torre 502 esq. Montero', '+598 2711 8603', '+598 97 043 191', 'info@francis.com.uy', 'Gastronomía', 'Montevideo', 'Uruguay', 'PROSPECCION_VERIFICADA'),
('10000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000001', 'Holiday Inn Montevideo / El Quijote', NULL, 'Colonia 823', '+598 2902 0001', NULL, 'reservas@himontevideo.com.uy', 'Hotelería / gastronomía', 'Montevideo', 'Uruguay', 'PROSPECCION_VERIFICADA'),
('10000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000001', 'Uruguay Pleno', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Uruguay', 'OUTBOUND'),
('10000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000001', 'Panda Cou', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Colombia', 'OUTBOUND_LATAM'),
('10000000-0000-0000-0000-000000000015', '00000000-0000-0000-0000-000000000001', 'Squania Suite', NULL, NULL, NULL, NULL, NULL, NULL, 'Termas del Daymán', 'Uruguay', 'SPECIAL'),
('10000000-0000-0000-0000-000000000016', '00000000-0000-0000-0000-000000000001', 'La Piojera', NULL, NULL, NULL, NULL, 'hola@lapiojera.cl', NULL, NULL, 'Chile', 'SPECIAL'),
('10000000-0000-0000-0000-000000000017', '00000000-0000-0000-0000-000000000001', 'Acruxs', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Perú', 'SPECIAL'),
('10000000-0000-0000-0000-000000000018', '00000000-0000-0000-0000-000000000001', 'Paramuno', NULL, NULL, NULL, NULL, NULL, NULL, NULL, 'Colombia', 'SPECIAL');

INSERT INTO contact (id, tenant_id, company_id, first_name, role, email, phone)
VALUES
('20000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'Ignacio', NULL, 'info@celisano.com.uy', '+598 92 244 124'),
('20000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000008', 'Gabriel Coquel', 'Dirección / gerencia', 'tandoryrestaurant@gmail.com', '+598 2709 6616'),
('20000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000011', 'Alberto Latarowski', NULL, 'info@francis.com.uy', '+598 2711 8603');

INSERT INTO lead (id, tenant_id, company_id, contact_id, source, temperature, status, next_contact_at, score, summary, priority, data_quality)
VALUES
('40000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'PROSPECCION_DIRECTA', 'WARM', 'NEW', '2026-08-21T12:00:00Z', 0, 'Visita presencial; hablar con Ignacio y conseguir reunión de diagnóstico. No cotizar antes del diagnóstico. Presentar Suture Agents como línea en desarrollo, no como producto terminado.', 'A+', 'HIGH'),
('40000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', NULL, 'REFERIDO_PERSONAL', 'WARM', 'NEW', NULL, 0, 'Primer contacto directo y cuidadoso con la dueña; entrar por problemas concretos de web/canal directo. Hotel grande. Existe contacto directo con la dueña, pero no guardar origen personal del número.', 'A+', 'HIGH'),
('40000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', NULL, 'PROSPECCION_DAYMAN', 'WARM', 'NEW', NULL, 0, 'Presentar propuesta orientada a canal directo; mencionar PMS/demo solo si aparece interés operativo.', 'A', 'HIGH'),
('40000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000004', NULL, 'PROSPECCION_DAYMAN', 'WARM', 'NEW', NULL, 0, 'Contactar al dueño por el número obtenido en recepción. Muy buena reputación online y oportunidad clara por canal web propio débil/ausente.', 'A', 'HIGH'),
('40000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', NULL, 'REFERRAL', 'HOT', 'PROPOSAL', NULL, 0, 'Validar alcance, revisar preview y definir aprobación del presupuesto. Plan recomendado en propuesta: Captación activa. Existen alternativas de UYU 25.000 y UYU 70.000.', 'A', 'HIGH'),
('40000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000006', NULL, 'PROSPECCION_VERIFICADA', 'COLD', 'NEW', NULL, 0, 'Llamar y pedir dueño, gerente o responsable comercial/digital.', 'A+', 'HIGH'),
('40000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000007', NULL, 'PROSPECCION_VERIFICADA', 'COLD', 'NEW', NULL, 0, 'WhatsApp o llamada buscando dueño, gerente o responsable web/digital.', 'A', 'HIGH'),
('40000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000002', 'PROSPECCION_VERIFICADA', 'COLD', 'NEW', NULL, 0, 'WhatsApp o llamada; pedir por Gabriel Coquel o dirección. NO vender rediseño web: ya tienen buen ecosistema digital.', 'B', 'HIGH'),
('40000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000009', NULL, 'PROSPECCION_VERIFICADA', 'COLD', 'NEW', NULL, 0, 'WhatsApp/email buscando propietarios, dirección o responsable comercial.', 'B', 'HIGH'),
('40000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000010', NULL, 'PROSPECCION_VERIFICADA', 'COLD', 'NEW', NULL, 0, 'Llamar o enviar email buscando dueño, gerencia u operaciones.', 'B', 'HIGH'),
('40000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000003', 'PROSPECCION_VERIFICADA', 'COLD', 'NEW', NULL, 0, 'Contactar profesionalmente y pedir por Alberto, dirección u operaciones/sistemas. NO vender web; ya tienen web, app, pedidos y soluciones corporativas.', 'C', 'HIGH'),
('40000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000012', NULL, 'PROSPECCION_VERIFICADA', 'COLD', 'NEW', NULL, 0, 'Llamar a recepción o enviar email buscando dirección, operaciones, F&B o sistemas. No vender web.', 'C', 'HIGH'),
('40000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000013', NULL, 'OUTBOUND', 'WARM', 'FOLLOW_UP', NULL, 0, 'Realizar seguimiento. Faltan recuperar contacto y oportunidad exacta antes de automatizar acciones.', 'B', 'INCOMPLETE'),
('40000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000014', NULL, 'OUTBOUND_LATAM', 'WARM', 'QUALIFIED', NULL, 0, 'Retomar conversación con jefe/decisor. Hubo respuesta positiva y la conversación llegó al jefe.', 'B', 'INCOMPLETE'),
('40000000-0000-0000-0000-000000000015', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000015', NULL, 'SPECIAL', 'COLD', 'LOST', NULL, 0, 'No vender; conservar como fuente de networking. Ofrecieron incorporación a un grupo de WhatsApp de la industria turística.', NULL, 'HIGH'),
('40000000-0000-0000-0000-000000000016', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000016', NULL, 'SPECIAL', 'COLD', 'CONTACT_DATA_INVALID', NULL, 0, 'Buscar contacto alternativo antes de continuar. Email rebotado: 550 no such user.', NULL, 'HIGH'),
('40000000-0000-0000-0000-000000000017', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000017', NULL, 'SPECIAL', 'COLD', 'LOST', NULL, 0, 'Rechazó propuesta de USD 350.', NULL, 'HIGH'),
('40000000-0000-0000-0000-000000000018', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000018', NULL, 'SPECIAL', 'COLD', 'LOST', NULL, 0, 'Proyecto de aproximadamente 90 productos quedó fuera de presupuesto.', NULL, 'HIGH');

INSERT INTO opportunity (id, tenant_id, company_id, contact_id, name, stage, estimated_value, currency, next_action, next_action_date, lost_reason)
VALUES
('30000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', '20000000-0000-0000-0000-000000000001', 'Software a medida, automatización de procesos y futura capa de agentes IA sobre datos operativos', 'NEW', NULL, 'UYU', 'Visita presencial; hablar con Ignacio y conseguir reunión de diagnóstico', '2026-08-21', NULL),
('30000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000002', NULL, 'Modernización web, reservas directas y potencial evolución hacia software hotelero + agente IA', 'NEW', NULL, 'UYU', 'Primer contacto directo y cuidadoso con la dueña; entrar por problemas concretos de web/canal directo', NULL, NULL),
('30000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000003', NULL, 'Web, reservas directas y potencial integración de software hotelero', 'NEW', NULL, 'UYU', 'Presentar propuesta orientada a canal directo; mencionar PMS/demo solo si aparece interés operativo', NULL, NULL),
('30000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000004', NULL, 'Web propia, reservas directas, reducción de dependencia de OTAs y futura automatización', 'NEW', NULL, 'UYU', 'Contactar al dueño por el número obtenido en recepción', NULL, NULL),
('30000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000005', NULL, 'Renovación web + sistema de captación comercial', 'PROPOSAL', 45000.00, 'UYU', 'Validar alcance, revisar preview y definir aprobación del presupuesto', NULL, NULL),
('30000000-0000-0000-0000-000000000006', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000006', NULL, 'Modernización web, mobile, menú, reservas directas, SEO local, analítica y posteriormente sistemas', 'NEW', NULL, 'UYU', 'Llamar y pedir dueño, gerente o responsable comercial/digital', NULL, NULL),
('30000000-0000-0000-0000-000000000007', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000007', NULL, 'Modernización web, SEO técnico, seguridad/indexación, reservas, menú y canal directo', 'NEW', NULL, 'UYU', 'WhatsApp o llamada buscando dueño, gerente o responsable web/digital', NULL, NULL),
('30000000-0000-0000-0000-000000000008', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000008', '20000000-0000-0000-0000-000000000002', 'Integraciones, automatización, CRM, reservas, WhatsApp y procesos internos', 'NEW', NULL, 'UYU', 'WhatsApp o llamada; pedir por Gabriel Coquel o dirección', NULL, NULL),
('30000000-0000-0000-0000-000000000009', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000009', NULL, 'Captación turística, automatización de consultas, eventos, CRM, multidioma e integraciones', 'NEW', NULL, 'UYU', 'WhatsApp/email buscando propietarios, dirección o responsable comercial', NULL, NULL),
('30000000-0000-0000-0000-000000000010', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000010', NULL, 'Reservas, eventos, CRM, automatizaciones e integración con operación', 'NEW', NULL, 'UYU', 'Llamar o enviar email buscando dueño, gerencia u operaciones', NULL, NULL),
('30000000-0000-0000-0000-000000000011', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000011', '20000000-0000-0000-0000-000000000003', 'Integraciones, reporting, CRM y automatización interna', 'NEW', NULL, 'UYU', 'Contactar profesionalmente y pedir por Alberto, dirección u operaciones/sistemas', NULL, NULL),
('30000000-0000-0000-0000-000000000012', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000012', NULL, 'Integración hotel-restaurante, eventos, reservas, automatización y software operativo', 'NEW', NULL, 'UYU', 'Llamar a recepción o enviar email buscando dirección, operaciones, F&B o sistemas', NULL, NULL),
('30000000-0000-0000-0000-000000000013', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000014', NULL, 'Desarrollo web / software', 'QUALIFIED', NULL, 'UYU', 'Retomar conversación con jefe/decisor', NULL, NULL),
('30000000-0000-0000-0000-000000000014', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000015', NULL, 'Cierre definitivo', 'LOST', NULL, 'UYU', 'No vender; conservar como fuente de networking', NULL, 'La empresa informó que estaba cerrando definitivamente'),
('30000000-0000-0000-0000-000000000015', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000017', NULL, 'Propuesta comercial', 'LOST', NULL, 'USD', NULL, NULL, 'Rechazó propuesta de USD 350'),
('30000000-0000-0000-0000-000000000016', '00000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000018', NULL, 'Proyecto de productos', 'LOST', NULL, 'USD', NULL, NULL, 'Proyecto de aproximadamente 90 productos quedó fuera de presupuesto');

INSERT INTO prospecting_exclusion (tenant_id, company_name)
VALUES
('00000000-0000-0000-0000-000000000001', 'Carnicería Don Juan (Pocitos)'),
('00000000-0000-0000-0000-000000000001', 'Panadería El Árbol (Pocitos)'),
('00000000-0000-0000-0000-000000000001', 'Dr. Germán Rodríguez - fisioterapia'),
('00000000-0000-0000-0000-000000000001', 'Pizzería La Esquina de Juancho'),
('00000000-0000-0000-0000-000000000001', 'Barbería El Barón (Carrasco)'),
('00000000-0000-0000-0000-000000000001', 'Carnicería La Rural (Carrasco)'),
('00000000-0000-0000-0000-000000000001', 'Dr. Sebastián Berruti - odontología'),
('00000000-0000-0000-0000-000000000001', 'Panadería Las Violetas (Malvín)');

CREATE TABLE task (
	tkey serial primary key,
    triggerkey character varying(255),
    triggername character varying(255),
    orgid character varying(255),
    orgname character varying(255),
    requestinguser character varying(255),
	description character varying(20000),
	comment character varying(8000),
	created timestamp,
	status_tkey int NOT NULL,
	approver_tkey int NOT NULL
);

CREATE TABLE status (
	tkey int primary key,
	name character varying(30)
);

CREATE TABLE approver (
	tkey serial primary key,
	userid character varying(255) NOT NULL UNIQUE
);

CREATE TABLE "version" (
		"productmajorversion" INTEGER NOT NULL, 
		"productminorversion" INTEGER NOT NULL, 
		"schemaversion" INTEGER NOT NULL, 
		"migrationdate" TIMESTAMP
);	

	
ALTER TABLE "task" ADD CONSTRAINT "task_status_fk" FOREIGN KEY ("status_tkey") REFERENCES "status" ("tkey");	
ALTER TABLE "task" ADD CONSTRAINT "task_approver_fk" FOREIGN KEY ("approver_tkey") REFERENCES "approver" ("tkey");	

INSERT INTO status VALUES (1,'WAITING_FOR_APPROVAL');
INSERT INTO status VALUES (2,'TIMEOUT');
INSERT INTO status VALUES (3,'FAILED');
INSERT INTO status VALUES (4,'APPROVED');
INSERT INTO status VALUES (5,'REJECTED');
INSERT INTO status VALUES (6,'NOTIFICATION');
INSERT INTO status VALUES (7,'WAITING_FOR_CLEARANCE');
INSERT INTO status VALUES (8,'CLEARANCE_GRANTED');


INSERT INTO approver VALUES (DEFAULT,'customer');

-- INSERT INTO task VALUES (1,'Windows 2008 R2', 'description 1', 'comment 1', CURRENT_TIMESTAMP, 1, 1);
-- INSERT INTO task VALUES (2,'Ubuntu Linux ', 'description 2', 'comment 2', CURRENT_TIMESTAMP, 1, 1);
-- INSERT INTO task VALUES (3,'CentOS', 'description 3', 'comment 3', CURRENT_TIMESTAMP, 1, 1);
GRANT ALL PRIVILEGES ON TABLE "task"  TO approvaluser;
GRANT ALL PRIVILEGES ON TABLE "status"  TO approvaluser;
GRANT ALL PRIVILEGES ON TABLE "version"  TO approvaluser;
GRANT ALL PRIVILEGES ON TABLE "approver"  TO approvaluser;
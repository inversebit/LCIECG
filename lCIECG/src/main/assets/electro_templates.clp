(defmodule TEMPLATES (export deftemplate ?ALL))

(deftemplate electro "length en ms, amplitude en mV"
	(slot BPM
		(type INTEGER))	
	(slot QRS-complex-amplitude
		(type FLOAT))
	(slot QRS-complex-length
		(type FLOAT))
	(slot RR-intval-length
		(type FLOAT))
	(slot P-amplitude
		(type FLOAT))
	(slot P-length
		(type FLOAT))
	(slot PR-intval-length
		(type FLOAT))
	(slot QTc-length
		(type FLOAT))
	(slot irregular-beats
		(type INTEGER)) ;If there have been 3 or more irreg beats
						;0 = none; 1=present, preceded by P; 2=present, not preceded by P
	(slot Q-amplitude
		(type FLOAT)) ;Valor absoluto de los mV
	(slot R-amplitude
		(type FLOAT))
	(slot P-difasic
		(type INTEGER)) ;0 = none; 1=pos then neg; 2 = neg then pos
	(slot U-amplitude
		(type FLOAT))
)

(deftemplate diagnostico
	(multislot patologias)
	(multislot razonamientos)
)


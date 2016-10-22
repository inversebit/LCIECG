;Unidades:
;	Tiempo en 	ms	(milisegundos)
;	Voltajes en nV 	(nanovoltios)

(defmodule INTERPRET-DATA (import MAIN ?ALL)(import TEMPLATES ?ALL)(export ?ALL))

(defrule tratar-pulso "Comprueba si el pulso esta fuera de los limites saludables"
	(electro (BPM ?frec &:(> ?frec 100)))
=>
	(if (> ?frec 100)
		then (assert (BPM demasiado-elevada))
		else (if (< ?frec 40)
				then (assert (BPM demasiado-baja))
			 )
	)
)

(defrule long-QRS-mayor-limite "Comprueba si la longitud del complejo QRS se encuentra por encima del limite adecuado"
	(electro (QRS-complex-length ?longQRS &:(> ?longQRS 120)))
=>
	(assert (QRS largo))
)

(defrule long-P-mayor-limite "Comprueba si la longitud de la onda P se encuentra por encima del limite adecuado"
	(electro (P-length ?longP &:(> ?longP 110)))
=>
	(assert (P largo))
)

(defrule ampli-P-mayor-limite "Comprueba si la amplitud de la onda P se encuentra por encima del limite adecuado"
	(electro (P-amplitude ?ampliP &:(> ?ampliP 250)))
=>
	(assert (P grande))
)

(defrule no-hay-P
	(test (> 0 1))
=>
	(assert (no-P))
)

(defrule ampli-Q-mayor-limite "Comprueba si la amplitud de la onda Q se encuentra por encima del limite adecuado"
	(electro (R-amplitude ?ampliR)(Q-amplitude ?ampliQ &:(> ?ampliQ (/ ?ampliR 3.0))))
=>
	(assert (Q grande))
)

(defrule tratar-ampli-U ""
	(electro (U-amplitude ?ampliU))
=>
	(if (< ?ampliU 0.2)
		then (assert (U normal))
		else (assert (U grande))
	)
)

(defrule tratar-P-difasica
	(electro (P-difasic ?difasic-type))
=>
	(if (<> ?difasic-type 0)
		then (assert (P-difasic ?difasic-type))
	)
)

(defrule tratar-ampli-T "Comprueba si la onda T es plana (muy baja) o invertida"
	(test (> 0 1))
;	(electro (T-amplitude ?ampliT))
=>
;	(if (< ?ampliT 0.2)
;		then (if (> ?ampliT 0.0)
;				then (assert (T plana))
;				else (assert (T invertida))
;			 )
;	)
)

(defrule tratar-long-QT "Comprueba si el segmento QT es demasiado corto o largo"
	(test (> 0 1))
;	(electro (QT-length ?lenQT))
=>
;	(if (< ?lenQT 380)
;		then (assert (QT corto))
;		else (if (> ?lenQT 440)
;				then (assert (QT largo))
;			 )
;	)
)

(defrule tratar-pulso-irreg
	(test (> 0 1))
;	(electro (irreg-beats ?irregBeats))
=>
;	(if (<> ?irregBeats 0)
;		then (assert (irreg-beat-type ?irregBeats))
;	)
)

(defrule long-PR-mayor-limite 
	(electro (PR-intval-length ?longPR &:(> ?longPR 200)))
=>
	(assert (PR largo))
)


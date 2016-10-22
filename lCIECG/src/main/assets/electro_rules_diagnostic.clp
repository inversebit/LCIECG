(defmodule GET-DIAGNOSTIC (import TEMPLATES ?ALL)(import FACTS ?ALL)(import INTERPRET-DATA ?ALL)(export ?ALL))

(defrule diagnosticar-estenosis-mitral "Comprueba si el paciente sufre estenosis mitral"
	?P-fact <- (P largo)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat estenosis-mitral)(razonamientos ?raz P-largo))
	(retract ?P-fact)
)

(defrule diagnosticar-hipertension-pulmonar "Comprueba si el paciente sufre hipertension pulmonar"
	?P-fact <- (P grande)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat hipertension-pulmonar)(razonamientos ?raz P-amplitud-elevada))
	(retract ?P-fact)
)

(defrule diagnosticar-bloqueo-rama "Comprueba si el paciente sufre de bloqueo de rama"
	?QRS-fact <-(QRS largo)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat bloqueo-de-rama)(razonamientos ?raz QRS-largo))
	(retract ?QRS-fact)
)

(defrule diangosticar-taquicardia
	?BPM-fact <-(BPM demasiado-elevada)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat taquicardia)(razonamientos ?raz pulso-elevado))
	(retract ?BPM-fact)
)

(defrule diagnosticar-Q-de-necrosis 
	?Q-fact <- (Q grande)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat infarto-lateral-arteria-circunfleja)(razonamientos ?raz ?Q-fact))
	(retract ?Q-fact)
)

(defrule diagnosticar-P-difasica
	?tipo-difasica <- (P-difasic ?difasic-type)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(if (= ?difasic-type 1)
		then (modify ?diag (patologias ?pat hipertrofia-auricular-derecha)(razonamientos ?raz T-difasica-positivo-despues-negativo))
		else (modify ?diag (patologias ?pat hipertrofia-auricular-izquierda)(razonamientos ?raz T-difasica-negativo-despues-positivo))
	)
	(retract ?tipo-difasica)
)

(defrule diagnosticar-ritmo-escape-ventricular1
	?BPM-baja <- (BPM demasiado-baja)
	?no-P <- (no-P)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat ritmo-de-escape-ventricular)(razonamientos ?raz Q-grande))
	(retract ?no-P)
	(retract ?BPM-baja)
)

(defrule diagnosticar-hipokalemia-moderada
	?U-norm <- (U normal)
	?T-plana <- (T plana)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat hipokalemia-moderada)(razonamientos ?raz ?U-norm ?T-plana))
	(retract ?U-norm)
	(retract ?T-plana)
)

(defrule diagnosticar-hipokalemia-extrema
	?U-grande <- (U grande)
	?T-inv <- (T invertida)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat hipokalemia-extrema)(razonamientos ?raz ?U-grande ?T-inv))
	(retract ?U-grande)
	(retract ?T-inv)
)

(defrule diangosticar-hipercalcemia
	?QT-corto <- (QT corto)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat hipercalcemia)(razonamientos ?raz ?QT-corto))
	(retract ?QT-corto)
)

(defrule diangosticar-hipocalcemia
	?QT-largo <- (QT largo)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat hipocalcemia)(razonamientos ?raz ?QT-largo))
	(retract ?QT-largo)
)

(defrule diagnsoticar-arritmia-sinusal
	?irreg-beat-W-P <- (irreg-beat-type 1)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat arritmia-sinusal)(razonamientos ?raz pulsos-irregulares-precedidos-de-P))
	(retract ?irreg-beat-W-P)
)

(defrule diagnsoticar-extrasistole
	?irreg-beat-WO-P <- (irreg-beat-type 2)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat extrasistole)(razonamientos ?raz pulsos-irregulares-no-precedidos-de-P))
	(retract ?irreg-beat-WO-P)
)

(defrule diagnosticar-bloqueo-de-conduccion-T1
	?PR-fact <-(PR largo)
	?diag <- (diagnostico (patologias $?pat)(razonamientos $?raz))
=>
	(modify ?diag (patologias ?pat bloqueo-de-conduccion-tipo1)(razonamientos ?raz ?PR-fact))
	(retract ?PR-fact)
)

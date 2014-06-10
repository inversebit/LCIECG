(defmodule FACTS (export ?ALL)(import TEMPLATES ?ALL))

(deffacts diag-paciente
	(diagnostico 
		(patologias nil)
		(razonamientos nil))
)


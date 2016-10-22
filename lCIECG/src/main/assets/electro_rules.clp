(defmodule MAIN (import FACTS ?ALL)(import TEMPLATES ?ALL)(export ?ALL))

(defrule MAIN::R-Presentacion ""
	(initial-fact)
=>
	(focus INTERPRET-DATA GET-DIAGNOSTIC)
)

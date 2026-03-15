{{- define "hello-svc.name" -}}
hello-svc
{{- end -}}

{{- define "hello-svc.fullname" -}}
{{- printf "%s-%s" .Release.Name (include "hello-svc.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "hello-svc.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
{{- if .Values.serviceAccount.name -}}
{{ .Values.serviceAccount.name }}
{{- else -}}
{{ include "hello-svc.fullname" . }}
{{- end -}}
{{- else -}}
default
{{- end -}}
{{- end -}}

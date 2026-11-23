---
category: Xochitepec
---
Notificación flotante (arriba a la derecha) para confirmar una acción o reportar un error; se cierra sola a los 6 segundos.

## Props

- `aviso` (`{ mensaje: string, tipo: "success" | "error" | "info" | "warning" } | null`): el aviso a mostrar; `null` lo oculta.
- `onClose` (requerido): limpia el aviso (normalmente `() => setAviso(null)`).

## Ejemplo

```jsx
const [aviso, setAviso] = useState(null);
// después de guardar:
setAviso({ tipo: "success", mensaje: "Solicitud enviada a Patrimonio para su autorización." });

<Aviso aviso={aviso} onClose={() => setAviso(null)} />
```

Colócalo una vez al final de la página. Los mensajes van en español, en una frase, con punto final.

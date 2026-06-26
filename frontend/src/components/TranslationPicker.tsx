import { TranslationOption } from '../api/vocabulary';

interface Props {
  options: TranslationOption[];
  selected: number[];
  onChange: (ids: number[]) => void;
  lang: string;
}

export function TranslationPicker({ options, selected, onChange }: Props) {
  function toggle(id: number) {
    onChange(
      selected.includes(id) ? selected.filter((x) => x !== id) : [...selected, id]
    );
  }

  return (
    <ul className="translation-picker">
      {options.map((opt) => (
        <li key={opt.id} className={`translation-option${selected.includes(opt.id) ? ' selected' : ''}`}>
          <button onClick={() => toggle(opt.id)} type="button">
            {opt.text}
          </button>
          <span className="provider-tag">{opt.provider}</span>
        </li>
      ))}
    </ul>
  );
}
